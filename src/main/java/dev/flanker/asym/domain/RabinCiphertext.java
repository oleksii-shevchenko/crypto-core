package dev.flanker.asym.domain;

import dev.flanker.alg.UnsignedInt;

public class RabinCiphertext {
    private final UnsignedInt y;
    private final UnsignedInt c1;
    private final UnsignedInt c2;

    private RabinCiphertext(UnsignedInt y, UnsignedInt c1, UnsignedInt c2) {
        this.y = y;
        this.c1 = c1;
        this.c2 = c2;
    }

    public static RabinCiphertext of(UnsignedInt y, UnsignedInt c1, UnsignedInt c2) {
        return new RabinCiphertext(y, c1, c2);
    }

    public static RabinCiphertext of(byte[] bytes) {
        return null;
    }

    public UnsignedInt getY() {
        return y;
    }

    public UnsignedInt getC1() {
        return c1;
    }

    public UnsignedInt getC2() {
        return c2;
    }

    public byte[] serialize() {
        return null;
    }
}
