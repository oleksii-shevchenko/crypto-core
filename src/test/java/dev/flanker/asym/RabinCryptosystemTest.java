package dev.flanker.asym;

import dev.flanker.alg.UnsignedInt;
import dev.flanker.asym.domain.RabinCiphertext;
import dev.flanker.asym.domain.RabinParameters;
import dev.flanker.rand.BbsRandom;
import dev.flanker.rand.Random;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class RabinCryptosystemTest {
    private static final UnsignedInt P = UnsignedInt.valueOf("D5BBB96D30086EC484EBA3D7F9CAEB07".toLowerCase());
    private static final UnsignedInt Q = UnsignedInt.valueOf("425D2B9BFDB25B9CF6C416CC6E37B59C1F".toLowerCase());

    private static final Random GENERATOR = BbsRandom.create(P, Q, UnsignedInt.random(256));
    private static final RabinCryptosystem CRYPTOSYSTEM = new RabinCryptosystem(GENERATOR);

    private static final int ITERATIONS = 8;

    @Test
    public void encryptionTest() {
        for (int i = 0; i < ITERATIONS; i++) {
            RabinParameters parameters = CRYPTOSYSTEM.generateKeys(512);
            UnsignedInt m = UnsignedInt.random(128);

            RabinCiphertext ciphertext = CRYPTOSYSTEM.encrypt(m, parameters.getPublicKey());
            UnsignedInt mPrime = CRYPTOSYSTEM.decrypt(ciphertext, parameters.getPrivateKey());

            assertEquals(m, mPrime);
        }
    }

    @Test
    public void signatureTest() {
        for (int i = 0; i < ITERATIONS; i++) {
            RabinParameters parameters = CRYPTOSYSTEM.generateKeys(512);
            UnsignedInt m = UnsignedInt.random(128);

            UnsignedInt signature = CRYPTOSYSTEM.sign(m, parameters.getPrivateKey());

            assertTrue(CRYPTOSYSTEM.verify(m, signature, parameters.getPublicKey()));

            int position = ThreadLocalRandom.current().nextInt(signature.bitLength());
            int bit = signature.getBit(position) ^ 1;
            signature = signature.setBit(bit, position);

            assertFalse(CRYPTOSYSTEM.verify(m, signature, parameters.getPublicKey())); // Probably should not
        }
    }

}