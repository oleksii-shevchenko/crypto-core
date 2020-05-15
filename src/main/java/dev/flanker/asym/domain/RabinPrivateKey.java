package dev.flanker.asym.domain;

import dev.flanker.alg.UnsignedInt;

public class RabinPrivateKey {
    private final UnsignedInt p;
    private final UnsignedInt q;
    private final UnsignedInt b;

    private RabinPrivateKey(UnsignedInt p, UnsignedInt q, UnsignedInt b) {
        this.p = p;
        this.q = q;
        this.b = b;
    }

    public static RabinPrivateKey of(UnsignedInt p, UnsignedInt q, UnsignedInt b) {
        return new RabinPrivateKey(p, q, b);
    }

    public UnsignedInt getP() {
        return p;
    }

    public UnsignedInt getQ() {
        return q;
    }

    public UnsignedInt getB() {
        return b;
    }
}
