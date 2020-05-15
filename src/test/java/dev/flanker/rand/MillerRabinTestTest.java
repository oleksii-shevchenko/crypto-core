package dev.flanker.rand;

import dev.flanker.alg.UnsignedInt;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class MillerRabinTestTest {
    private static final int ITERATIONS = 2048;

    @Test
    public void primeTest() {
        BigInteger bi = new BigInteger(128, ThreadLocalRandom.current()).setBit(0);
        UnsignedInt ui = UnsignedInt.valueOf(bi.toString(16));

        for (int i = 0; i < ITERATIONS; i++) {
            assertEquals(bi.isProbablePrime(32), MillerRabinTest.isPrime(ui));
            bi = bi.add(BigInteger.TWO);
            ui = ui.add(UnsignedInt.TWO);
        }
    }
}