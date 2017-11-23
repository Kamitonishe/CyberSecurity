package ru.mipt.cybersecurity.pqc.jcajce.provider.test;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;

import ru.mipt.cybersecurity.pqc.asn1.PQCObjectIdentifiers;
import ru.mipt.cybersecurity.pqc.jcajce.spec.McElieceCCA2KeyGenParameterSpec;


public class McElieceCCA2KeyPairGeneratorTest
    extends KeyPairGeneratorTest
{

    protected void setUp()
    {
        super.setUp();
    }

    public void testKeyFactory()
        throws Exception
    {
        kf = KeyFactory.getInstance("McElieceKobaraImai");
        kf = KeyFactory.getInstance("McEliecePointcheval");
        kf = KeyFactory.getInstance("McElieceFujisaki");
        kf = KeyFactory.getInstance(PQCObjectIdentifiers.mcElieceCca2.getId());
    }

    public void testKeyPairEncoding_9_33()
        throws Exception
    {
        kf = KeyFactory.getInstance(PQCObjectIdentifiers.mcElieceCca2.getId());

        kpg = KeyPairGenerator.getInstance("McElieceKobaraImai");
        McElieceCCA2KeyGenParameterSpec params = new McElieceCCA2KeyGenParameterSpec(9, 33);
        kpg.initialize(params);
        performKeyPairEncodingTest(kpg.generateKeyPair());
    }
}
