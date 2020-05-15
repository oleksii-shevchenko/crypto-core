package dev.flanker.rand;

import dev.flanker.alg.UnsignedInt;

public class BbsRandom implements Random {
    private final UnsignedInt module;

    private  UnsignedInt r;

    private BbsRandom(UnsignedInt module, UnsignedInt r) {
        this.module = module;
        this.r = r;
    }

    public static BbsRandom create(UnsignedInt n, UnsignedInt r) {
        return new BbsRandom(n, r);
    }

    public static BbsRandom create(UnsignedInt p, UnsignedInt q, UnsignedInt r) {
        return new BbsRandom(p.multiply(q), r);
    }


    @Override
    public void generate(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            int b = 0;
            for (int j = 0; j < Byte.SIZE; j++) {
                r = r.sqr(module);
                b = b ^ (r.getBit(0) << j);
            }
            bytes[i] = (byte) b;
        }
    }

    @Override
    public int nextInt() {
        int rand = 0;
        for (int i = 0; i < Integer.SIZE; i++) {
            r = r.sqr(module);
            rand = rand ^ (r.getBit(0) << i);
        }
        return rand;
    }
}
