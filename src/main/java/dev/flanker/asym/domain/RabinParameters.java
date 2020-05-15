package dev.flanker.asym.domain;

import dev.flanker.alg.UnsignedInt;

public class RabinParameters {
    private final RabinPrivateKey privateKey;
    private final RabinPublicKey publicKey;

    private RabinParameters(UnsignedInt p, UnsignedInt q, UnsignedInt b, UnsignedInt n) {
        this.privateKey = RabinPrivateKey.of(p, q, b);
        this.publicKey = RabinPublicKey.of(n, b);
    }

    public static RabinParameters of(UnsignedInt p, UnsignedInt q, UnsignedInt b, UnsignedInt n) {
        return new RabinParameters(p, q, b, n);
    }

    public RabinPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RabinPublicKey getPublicKey() {
        return publicKey;
    }
}
