package dev.flanker.rand;

import dev.flanker.alg.UnsignedInt;

public final class MillerRabinTest {
    private static final int ITERATIONS = 32;

    private MillerRabinTest() {}

    public static boolean isPrime(UnsignedInt n) {
        if (n.getBit(0) == 0) {
            return false;
        }

        UnsignedInt d = n.setBit(0, 0);

        int r = 0;
        while (d.getBit(0) == 0) {
            d = d.shiftRight(1);
            r++;
        }

        UnsignedInt limit = n.subtract(UnsignedInt.valueOf(3));
        UnsignedInt negativeOne = n.subtract(UnsignedInt.ONE);

        OUTER:
        for (int i = 0; i < ITERATIONS; i++) {
            UnsignedInt a = UnsignedInt.random(n.bitLength()).mod(limit).add(UnsignedInt.TWO);
            UnsignedInt x = a.pow(d, n);
            if (x.equals(UnsignedInt.ONE) || x.equals(negativeOne)) {
                continue;
            }
            for (int j = 0; j < r - 1; j++) {
                x = x.sqr(n);
                if (x.equals(negativeOne)) {
                    continue OUTER;
                }
            }
            return false;
        }
        return true;
    }
}
