package ru.mipt.cybersecurity.crypto.signers;

import java.math.BigInteger;
import java.security.SecureRandom;

import ru.mipt.cybersecurity.crypto.CipherParameters;
import ru.mipt.cybersecurity.crypto.DSA;
import ru.mipt.cybersecurity.crypto.params.ECDomainParameters;
import ru.mipt.cybersecurity.crypto.params.ECKeyParameters;
import ru.mipt.cybersecurity.crypto.params.ECPrivateKeyParameters;
import ru.mipt.cybersecurity.crypto.params.ECPublicKeyParameters;
import ru.mipt.cybersecurity.crypto.params.ParametersWithRandom;
import ru.mipt.cybersecurity.math.ec.ECAlgorithms;
import ru.mipt.cybersecurity.math.ec.ECCurve;
import ru.mipt.cybersecurity.math.ec.ECFieldElement;
import ru.mipt.cybersecurity.math.ec.ECMultiplier;
import ru.mipt.cybersecurity.math.ec.ECPoint;
import ru.mipt.cybersecurity.math.ec.FixedPointCombMultiplier;
import ru.mipt.cybersecurity.util.Arrays;

/**
 * DSTU 4145-2002
 * <p>
 * National Ukrainian standard of digital signature based on elliptic curves (DSTU 4145-2002).
 * </p>
 */
public class DSTU4145Signer
    implements DSA
{
    private static final BigInteger ONE = BigInteger.valueOf(1);

    private ECKeyParameters key;
    private SecureRandom random;

    public void init(boolean forSigning, CipherParameters param)
    {
        if (forSigning)
        {
            if (param instanceof ParametersWithRandom)
            {
                ParametersWithRandom rParam = (ParametersWithRandom)param;

                this.random = rParam.getRandom();
                param = rParam.getParameters();
            }
            else
            {
                this.random = new SecureRandom();
            }

            this.key = (ECPrivateKeyParameters)param;
        }
        else
        {
            this.key = (ECPublicKeyParameters)param;
        }

    }

    public BigInteger[] generateSignature(byte[] message)
    {
        ECDomainParameters ec = key.getParameters();

        ECCurve curve = ec.getCurve();

        ECFieldElement h = hash2FieldElement(curve, message);
        if (h.isZero())
        {
            h = curve.fromBigInteger(ONE);
        }

        BigInteger n = ec.getN();
        BigInteger e, r, s;
        ECFieldElement Fe, y;

        BigInteger d = ((ECPrivateKeyParameters)key).getD();

        ECMultiplier basePointMultiplier = createBasePointMultiplier();

        do
        {
            do
            {
                do
                {
                    e = generateRandomInteger(n, random);
                    Fe = basePointMultiplier.multiply(ec.getG(), e).normalize().getAffineXCoord();
                }
                while (Fe.isZero());

                y = h.multiply(Fe);
                r = fieldElement2Integer(n, y);
            }
            while (r.signum() == 0);

            s = r.multiply(d).add(e).mod(n);
        }
        while (s.signum() == 0);

        return new BigInteger[]{r, s};
    }

    public boolean verifySignature(byte[] message, BigInteger r, BigInteger s)
    {
        if (r.signum() <= 0 || s.signum() <= 0)
        {
            return false;
        }

        ECDomainParameters parameters = key.getParameters();

        BigInteger n = parameters.getN();
        if (r.compareTo(n) >= 0 || s.compareTo(n) >= 0)
        {
            return false;
        }

        ECCurve curve = parameters.getCurve();

        ECFieldElement h = hash2FieldElement(curve, message);
        if (h.isZero())
        {
            h = curve.fromBigInteger(ONE);
        }

        ECPoint R = ECAlgorithms.sumOfTwoMultiplies(parameters.getG(), s, ((ECPublicKeyParameters)key).getQ(), r).normalize();

        // components must be bogus.
        if (R.isInfinity())
        {
            return false;
        }

        ECFieldElement y = h.multiply(R.getAffineXCoord());
        return fieldElement2Integer(n, y).compareTo(r) == 0;
    }

    protected ECMultiplier createBasePointMultiplier()
    {
        return new FixedPointCombMultiplier();
    }

    /**
     * Generates random integer such, than its bit length is less than that of n
     */
    private static BigInteger generateRandomInteger(BigInteger n, SecureRandom random)
    {
        return new BigInteger(n.bitLength() - 1, random);
    }

    private static ECFieldElement hash2FieldElement(ECCurve curve, byte[] hash)
    {
        byte[] data = Arrays.reverse(hash);
        return curve.fromBigInteger(truncate(new BigInteger(1, data), curve.getFieldSize()));
    }

    private static BigInteger fieldElement2Integer(BigInteger n, ECFieldElement fe)
    {
        return truncate(fe.toBigInteger(), n.bitLength() - 1);
    }

    private static BigInteger truncate(BigInteger x, int bitLength)
    {
        if (x.bitLength() > bitLength)
        {
            x = x.mod(ONE.shiftLeft(bitLength));
        }
        return x;
    }
}
