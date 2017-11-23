package ru.mipt.cybersecurity.jce;

import java.util.Enumeration;

import ru.mipt.cybersecurity.asn1.ASN1ObjectIdentifier;
import ru.mipt.cybersecurity.asn1.x9.X9ECParameters;
import ru.mipt.cybersecurity.jce.spec.ECNamedCurveParameterSpec;

/**
 * a table of locally supported named curves.
 */
public class ECNamedCurveTable
{
    /**
     * return a parameter spec representing the passed in named
     * curve. The routine returns null if the curve is not present.
     * 
     * @param name the name of the curve requested
     * @return a parameter spec for the curve, null if it is not available.
     */
    public static ECNamedCurveParameterSpec getParameterSpec(
        String  name)
    {
        X9ECParameters  ecP = ru.mipt.cybersecurity.crypto.ec.CustomNamedCurves.getByName(name);
        if (ecP == null)
        {
            try
            {
                ecP = ru.mipt.cybersecurity.crypto.ec.CustomNamedCurves.getByOID(new ASN1ObjectIdentifier(name));
            }
            catch (IllegalArgumentException e)
            {
                // ignore - not an oid
            }

            if (ecP == null)
            {
                ecP = ru.mipt.cybersecurity.asn1.x9.ECNamedCurveTable.getByName(name);
                if (ecP == null)
                {
                    try
                    {
                        ecP = ru.mipt.cybersecurity.asn1.x9.ECNamedCurveTable.getByOID(new ASN1ObjectIdentifier(name));
                    }
                    catch (IllegalArgumentException e)
                    {
                        // ignore - not an oid
                    }
                }
            }
        }

        if (ecP == null)
        {
            return null;
        }

        return new ECNamedCurveParameterSpec(
                                        name,
                                        ecP.getCurve(),
                                        ecP.getG(),
                                        ecP.getN(),
                                        ecP.getH(),
                                        ecP.getSeed());
    }

    /**
     * return an enumeration of the names of the available curves.
     *
     * @return an enumeration of the names of the available curves.
     */
    public static Enumeration getNames()
    {
        return ru.mipt.cybersecurity.asn1.x9.ECNamedCurveTable.getNames();
    }
}
