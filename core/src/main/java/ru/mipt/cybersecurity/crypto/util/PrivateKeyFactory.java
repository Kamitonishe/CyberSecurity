package ru.mipt.cybersecurity.crypto.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import ru.mipt.cybersecurity.asn1.ASN1Encodable;
import ru.mipt.cybersecurity.asn1.ASN1InputStream;
import ru.mipt.cybersecurity.asn1.ASN1Integer;
import ru.mipt.cybersecurity.asn1.ASN1ObjectIdentifier;
import ru.mipt.cybersecurity.asn1.ASN1Primitive;
import ru.mipt.cybersecurity.asn1.oiw.ElGamalParameter;
import ru.mipt.cybersecurity.asn1.oiw.OIWObjectIdentifiers;
import ru.mipt.cybersecurity.asn1.pkcs.DHParameter;
import ru.mipt.cybersecurity.asn1.pkcs.PKCSObjectIdentifiers;
import ru.mipt.cybersecurity.asn1.pkcs.PrivateKeyInfo;
import ru.mipt.cybersecurity.asn1.pkcs.RSAPrivateKey;
import ru.mipt.cybersecurity.asn1.sec.ECPrivateKey;
import ru.mipt.cybersecurity.asn1.x509.AlgorithmIdentifier;
import ru.mipt.cybersecurity.asn1.x509.DSAParameter;
import ru.mipt.cybersecurity.asn1.x9.ECNamedCurveTable;
import ru.mipt.cybersecurity.asn1.x9.X962Parameters;
import ru.mipt.cybersecurity.asn1.x9.X9ECParameters;
import ru.mipt.cybersecurity.asn1.x9.X9ObjectIdentifiers;
import ru.mipt.cybersecurity.crypto.ec.CustomNamedCurves;
import ru.mipt.cybersecurity.crypto.params.AsymmetricKeyParameter;
import ru.mipt.cybersecurity.crypto.params.DHParameters;
import ru.mipt.cybersecurity.crypto.params.DHPrivateKeyParameters;
import ru.mipt.cybersecurity.crypto.params.DSAParameters;
import ru.mipt.cybersecurity.crypto.params.DSAPrivateKeyParameters;
import ru.mipt.cybersecurity.crypto.params.ECDomainParameters;
import ru.mipt.cybersecurity.crypto.params.ECNamedDomainParameters;
import ru.mipt.cybersecurity.crypto.params.ECPrivateKeyParameters;
import ru.mipt.cybersecurity.crypto.params.ElGamalParameters;
import ru.mipt.cybersecurity.crypto.params.ElGamalPrivateKeyParameters;
import ru.mipt.cybersecurity.crypto.params.RSAPrivateCrtKeyParameters;

/**
 * Factory for creating private key objects from PKCS8 PrivateKeyInfo objects.
 */
public class PrivateKeyFactory
{
    /**
     * Create a private key parameter from a PKCS8 PrivateKeyInfo encoding.
     * 
     * @param privateKeyInfoData the PrivateKeyInfo encoding
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(byte[] privateKeyInfoData) throws IOException
    {
        return createKey(PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(privateKeyInfoData)));
    }

    /**
     * Create a private key parameter from a PKCS8 PrivateKeyInfo encoding read from a
     * stream.
     * 
     * @param inStr the stream to read the PrivateKeyInfo encoding from
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(InputStream inStr) throws IOException
    {
        return createKey(PrivateKeyInfo.getInstance(new ASN1InputStream(inStr).readObject()));
    }

    /**
     * Create a private key parameter from the passed in PKCS8 PrivateKeyInfo object.
     * 
     * @param keyInfo the PrivateKeyInfo object containing the key material
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(PrivateKeyInfo keyInfo) throws IOException
    {
        AlgorithmIdentifier algId = keyInfo.getPrivateKeyAlgorithm();

        if (algId.getAlgorithm().equals(PKCSObjectIdentifiers.rsaEncryption))
        {
            RSAPrivateKey keyStructure = RSAPrivateKey.getInstance(keyInfo.parsePrivateKey());

            return new RSAPrivateCrtKeyParameters(keyStructure.getModulus(),
                keyStructure.getPublicExponent(), keyStructure.getPrivateExponent(),
                keyStructure.getPrime1(), keyStructure.getPrime2(), keyStructure.getExponent1(),
                keyStructure.getExponent2(), keyStructure.getCoefficient());
        }
        // TODO?
//      else if (algId.getObjectId().equals(X9ObjectIdentifiers.dhpublicnumber))
        else if (algId.getAlgorithm().equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
            DHParameter params = DHParameter.getInstance(algId.getParameters());
            ASN1Integer derX = (ASN1Integer)keyInfo.parsePrivateKey();

            BigInteger lVal = params.getL();
            int l = lVal == null ? 0 : lVal.intValue();
            DHParameters dhParams = new DHParameters(params.getP(), params.getG(), null, l);

            return new DHPrivateKeyParameters(derX.getValue(), dhParams);
        }
        else if (algId.getAlgorithm().equals(OIWObjectIdentifiers.elGamalAlgorithm))
        {
            ElGamalParameter params = ElGamalParameter.getInstance(algId.getParameters());
            ASN1Integer derX = (ASN1Integer)keyInfo.parsePrivateKey();

            return new ElGamalPrivateKeyParameters(derX.getValue(), new ElGamalParameters(
                params.getP(), params.getG()));
        }
        else if (algId.getAlgorithm().equals(X9ObjectIdentifiers.id_dsa))
        {
            ASN1Integer derX = (ASN1Integer)keyInfo.parsePrivateKey();
            ASN1Encodable de = algId.getParameters();

            DSAParameters parameters = null;
            if (de != null)
            {
                DSAParameter params = DSAParameter.getInstance(de.toASN1Primitive());
                parameters = new DSAParameters(params.getP(), params.getQ(), params.getG());
            }

            return new DSAPrivateKeyParameters(derX.getValue(), parameters);
        }
        else if (algId.getAlgorithm().equals(X9ObjectIdentifiers.id_ecPublicKey))
        {
            X962Parameters params = new X962Parameters((ASN1Primitive)algId.getParameters());

            X9ECParameters x9;
            ECDomainParameters dParams;

            if (params.isNamedCurve())
            {
                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier)params.getParameters();

                x9 = CustomNamedCurves.getByOID(oid);
                if (x9 == null)
                {
                    x9 = ECNamedCurveTable.getByOID(oid);
                }
                dParams = new ECNamedDomainParameters(
                    oid, x9.getCurve(), x9.getG(), x9.getN(), x9.getH(), x9.getSeed());
            }
            else
            {
                x9 = X9ECParameters.getInstance(params.getParameters());
                dParams = new ECDomainParameters(
                    x9.getCurve(), x9.getG(), x9.getN(), x9.getH(), x9.getSeed());
            }

            ECPrivateKey ec = ECPrivateKey.getInstance(keyInfo.parsePrivateKey());
            BigInteger d = ec.getKey();

            return new ECPrivateKeyParameters(d, dParams);
        }
        else
        {
            throw new RuntimeException("algorithm identifier in key not recognised");
        }
    }
}