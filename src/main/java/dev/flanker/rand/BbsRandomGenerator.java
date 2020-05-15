package dev.flanker.rand;

import dev.flanker.alg.UnsignedInt;

public class BbsRandomGenerator implements RandomGenerator {
    private final UnsignedInt module;

    private BbsRandomGenerator(UnsignedInt module) {
        this.module = module;
    }

    public static BbsRandomGenerator create(UnsignedInt n) {
        return new BbsRandomGenerator(n);
    }

    public static BbsRandomGenerator create(UnsignedInt p, UnsignedInt q) {
        return new BbsRandomGenerator(p.multiply(q));
    }


    @Override
    public void generate(byte[] bytes) {
        UnsignedInt r = UnsignedInt.random(module.bitLength());
        for (int i = 0; i < bytes.length; i++) {
            int b = 0;
            for (int j = 0; j < Byte.SIZE; j++) {
                r = r.sqr(module);
                b = b ^ (r.getBit(0) << j);
            }
            bytes[i] = (byte) b;
        }
    }
}
