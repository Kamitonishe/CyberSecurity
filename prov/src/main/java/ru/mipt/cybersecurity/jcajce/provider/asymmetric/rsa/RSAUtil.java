package ru.mipt.cybersecurity.jcajce.provider.asymmetric.rsa;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import ru.mipt.cybersecurity.asn1.ASN1ObjectIdentifier;
import ru.mipt.cybersecurity.asn1.pkcs.PKCSObjectIdentifiers;
import ru.mipt.cybersecurity.asn1.x509.X509ObjectIdentifiers;
import ru.mipt.cybersecurity.crypto.params.RSAKeyParameters;
import ru.mipt.cybersecurity.crypto.params.RSAPrivateCrtKeyParameters;
import ru.mipt.cybersecurity.util.Arrays;
import ru.mipt.cybersecurity.util.Fingerprint;

/**
 * utility class for converting java.security RSA objects into their
 * ru.mipt.cybersecurity.crypto counterparts.
 */
public class RSAUtil
{
    public static final ASN1ObjectIdentifier[] rsaOids =
    {
        PKCSObjectIdentifiers.rsaEncryption,
        X509ObjectIdentifiers.id_ea_rsa,
        PKCSObjectIdentifiers.id_RSAES_OAEP,
        PKCSObjectIdentifiers.id_RSASSA_PSS
    };

    public static boolean isRsaOid(
        ASN1ObjectIdentifier algOid)
    {
        for (int i = 0; i != rsaOids.length; i++)
        {
            if (algOid.equals(rsaOids[i]))
            {
                return true;
            }
        }

        return false;
    }

    static RSAKeyParameters generatePublicKeyParameter(
        RSAPublicKey key)
    {
        return new RSAKeyParameters(false, key.getModulus(), key.getPublicExponent());

    }

    static RSAKeyParameters generatePrivateKeyParameter(
        RSAPrivateKey key)
    {
        if (key instanceof RSAPrivateCrtKey)
        {
            RSAPrivateCrtKey k = (RSAPrivateCrtKey)key;

            return new RSAPrivateCrtKeyParameters(k.getModulus(),
                k.getPublicExponent(), k.getPrivateExponent(),
                k.getPrimeP(), k.getPrimeQ(), k.getPrimeExponentP(), k.getPrimeExponentQ(), k.getCrtCoefficient());
        }
        else
        {
            RSAPrivateKey k = key;

            return new RSAKeyParameters(true, k.getModulus(), k.getPrivateExponent());
        }
    }

    static String generateKeyFingerprint(BigInteger modulus, BigInteger publicExponent)
    {
        return new Fingerprint(Arrays.concatenate(modulus.toByteArray(), publicExponent.toByteArray())).toString();
    }
}
