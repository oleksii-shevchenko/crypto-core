package dev.flanker.asym.domain;

import dev.flanker.alg.UnsignedInt;

public class RabinPublicKey {
    private final UnsignedInt n;
    private final UnsignedInt b;

    private RabinPublicKey(UnsignedInt n, UnsignedInt b) {
        this.n = n;
        this.b = b;
    }
    
    public static RabinPublicKey of(UnsignedInt n, UnsignedInt b) {
        return new RabinPublicKey(n, b);
    }

    public UnsignedInt getN() {
        return n;
    }

    public UnsignedInt getB() {
        return b;
    }
}
