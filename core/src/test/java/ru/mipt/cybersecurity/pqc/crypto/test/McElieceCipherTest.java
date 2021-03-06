package ru.mipt.cybersecurity.pqc.crypto.test;

import java.security.SecureRandom;
import java.util.Random;

import ru.mipt.cybersecurity.crypto.AsymmetricCipherKeyPair;
import ru.mipt.cybersecurity.crypto.Digest;
import ru.mipt.cybersecurity.crypto.InvalidCipherTextException;
import ru.mipt.cybersecurity.crypto.digests.SHA256Digest;
import ru.mipt.cybersecurity.crypto.params.ParametersWithRandom;
import ru.mipt.cybersecurity.pqc.crypto.mceliece.McElieceCipher;
import ru.mipt.cybersecurity.pqc.crypto.mceliece.McElieceKeyGenerationParameters;
import ru.mipt.cybersecurity.pqc.crypto.mceliece.McElieceKeyPairGenerator;
import ru.mipt.cybersecurity.pqc.crypto.mceliece.McElieceParameters;
import ru.mipt.cybersecurity.util.test.SimpleTest;

public class McElieceCipherTest
    extends SimpleTest
{

    SecureRandom keyRandom = new SecureRandom();

    public String getName()
    {
        return "McEliecePKCS";

    }


    public void performTest()
        throws InvalidCipherTextException
    {
        int numPassesKPG = 1;
        int numPassesEncDec = 10;
        Random rand = new Random();
        byte[] mBytes;
        for (int j = 0; j < numPassesKPG; j++)
        {

            McElieceParameters params = new McElieceParameters();
            McElieceKeyPairGenerator mcElieceKeyGen = new McElieceKeyPairGenerator();
            McElieceKeyGenerationParameters genParam = new McElieceKeyGenerationParameters(keyRandom, params);

            mcElieceKeyGen.init(genParam);
            AsymmetricCipherKeyPair pair = mcElieceKeyGen.generateKeyPair();

            ParametersWithRandom param = new ParametersWithRandom(pair.getPublic(), keyRandom);
            Digest msgDigest = new SHA256Digest();
            McElieceCipher mcEliecePKCSDigestCipher = new McElieceCipher();


            for (int k = 1; k <= numPassesEncDec; k++)
            {
                System.out.println("############### test: " + k);
                // initialize for encryption
                mcEliecePKCSDigestCipher.init(true, param);

                // generate random message
                int mLength = (rand.nextInt() & 0x1f) + 1;
                mBytes = new byte[mLength];
                rand.nextBytes(mBytes);

                // encrypt
                msgDigest.update(mBytes, 0, mBytes.length);
                byte[] hash = new byte[msgDigest.getDigestSize()];

                msgDigest.doFinal(hash, 0);

                byte[] enc = mcEliecePKCSDigestCipher.messageEncrypt(hash);

                // initialize for decryption
                mcEliecePKCSDigestCipher.init(false, pair.getPrivate());
                byte[] constructedmessage = mcEliecePKCSDigestCipher.messageDecrypt(enc);

                boolean verified = true;
                for (int i = 0; i < hash.length; i++)
                {
                    verified = verified && hash[i] == constructedmessage[i];
                }

                if (!verified)
                {
                    fail("en/decryption fails");
                }
                else
                {
                    System.out.println("test okay");
                    System.out.println();
                }

            }
        }

    }

    public static void main(
        String[] args)
    {
        runTest(new McElieceCipherTest());
    }

}
