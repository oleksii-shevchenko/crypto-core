package dev.flanker.asym;

import dev.flanker.alg.UnsignedInt;
import dev.flanker.rand.BbsRandom;
import dev.flanker.rand.Random;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RabinUtilTest {
    private static final int ITERATIONS = 8;

    private static final UnsignedInt P = UnsignedInt.valueOf("D5BBB96D30086EC484EBA3D7F9CAEB07".toLowerCase());
    private static final UnsignedInt Q = UnsignedInt.valueOf("425D2B9BFDB25B9CF6C416CC6E37B59C1F".toLowerCase());
    private static final UnsignedInt N = P.multiply(Q);

    private static final Random GENERATOR = BbsRandom.create(P, Q, UnsignedInt.random(128));

    @Test
    @Ignore
    public void blumeTest() {
        System.out.println(RabinUtil.generateBlumePrime(512, GENERATOR));
    }

    @Test
    public void formatterTest() {
        for (int i = 0; i < ITERATIONS; i++) {
            UnsignedInt p = RabinUtil.generateBlumePrime(256, GENERATOR);
            UnsignedInt q = RabinUtil.generateBlumePrime(256, GENERATOR);
            UnsignedInt n = p.multiply(q);

            UnsignedInt m = UnsignedInt.random(256);

            assertEquals(m, RabinUtil.deformedMessage(RabinUtil.formatMessage(m, n, GENERATOR), n));
        }
    }

    @Test
    public void jacobiTest() {
        for (int i = 0; i < ITERATIONS; i++) {
            UnsignedInt x = UnsignedInt.random(1024);
            x = x.sqr(N);
            assertEquals(1, RabinUtil.jacobiSymbol(x, N));
        }
    }

}