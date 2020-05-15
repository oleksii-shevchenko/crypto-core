package dev.flanker.asym;

import dev.flanker.alg.UnsignedInt;
import dev.flanker.asym.domain.RabinCiphertext;
import dev.flanker.asym.domain.RabinParameters;
import dev.flanker.asym.domain.RabinPrivateKey;
import dev.flanker.asym.domain.RabinPublicKey;
import dev.flanker.rand.Random;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static dev.flanker.asym.RabinUtil.jacobiSymbol;

public class RabinCryptosystem {
    private static final int ROOTS_NUMBER = 4;

    private final Random random;

    public RabinCryptosystem(Random random) {
        this.random = random;
    }

    public RabinParameters generateKeys(int bitLength) {
        UnsignedInt p, q, n, b;
        p = RabinUtil.generateBlumePrime(bitLength >> 1, random);
        q = RabinUtil.generateBlumePrime(bitLength >> 1, random);
        n = p.multiply(q);
        b = UnsignedInt.random(n.bitLength(), random).mod(n);
        return RabinParameters.of(p, q, b, n);
    }

    public RabinCiphertext encrypt(UnsignedInt m, RabinPublicKey publicKey) {
        m = RabinUtil.formatMessage(m, publicKey.getN(), random);

        UnsignedInt y = m.multiply(m.add(publicKey.getB()), publicKey.getN());

        UnsignedInt inverseTwo = UnsignedInt.TWO.modInverse(publicKey.getN());
        UnsignedInt x = m.add(publicKey.getB().multiply(inverseTwo), publicKey.getN());

        UnsignedInt c1 = x.mod(UnsignedInt.TWO);
        UnsignedInt c2 = jacobiSymbol(x, publicKey.getN()) == 1 ? UnsignedInt.ONE : UnsignedInt.ZERO;

        return RabinCiphertext.of(y, c1, c2);
    }

    public static UnsignedInt decrypt(RabinCiphertext c, RabinPrivateKey privateKey) {
        UnsignedInt p = privateKey.getP();
        UnsignedInt q = privateKey.getQ();
        UnsignedInt b = privateKey.getB();

        UnsignedInt y = c.getY();

        UnsignedInt n = p.multiply(q);
        UnsignedInt b_2 = b.multiply(UnsignedInt.TWO.modInverse(n)).mod(n);

        UnsignedInt yp = y.add(b_2.pow(UnsignedInt.TWO, n), n).pow(p.add(UnsignedInt.ONE).shiftRight(2), p);
        UnsignedInt yq = y.add(b_2.pow(UnsignedInt.TWO, n), n).pow(q.add(UnsignedInt.ONE).shiftRight(2), q);

        List<UnsignedInt> roots = RabinUtil.composeRoots(yp, p, yq, q);
        for (UnsignedInt root : roots) {
            UnsignedInt c1 = root.mod(UnsignedInt.TWO);
            UnsignedInt c2 = jacobiSymbol(root, n) == 1 ? UnsignedInt.ONE : UnsignedInt.ZERO;
            if (c.getC1().equals(c1) && c.getC2().equals(c2)) {
                UnsignedInt x = n.subtract(b_2).add(root).mod(n);
                return RabinUtil.deformedMessage(x, n);
            }
        }
        return null;
    }

    public UnsignedInt sign(UnsignedInt x, RabinPrivateKey privateKey) {
        UnsignedInt p = privateKey.getP();
        UnsignedInt q = privateKey.getQ();

        UnsignedInt n = p.multiply(q);

        UnsignedInt formattedX = RabinUtil.formatMessage(x, n, random);
        while (jacobiSymbol(formattedX, p) + jacobiSymbol(formattedX, q) != 2) {
            formattedX = RabinUtil.formatMessage(x, n, random);
        }

        UnsignedInt xp = formattedX.pow(p.add(UnsignedInt.ONE).shiftRight(2), p);
        UnsignedInt xq = formattedX.pow(q.add(UnsignedInt.ONE).shiftRight(2), q);

        return RabinUtil.composeRoots(xp, p, xq, q).get(ThreadLocalRandom.current().nextInt(ROOTS_NUMBER));
    }

    public boolean verify(UnsignedInt x, UnsignedInt s, RabinPublicKey publicKey) {
        UnsignedInt n = publicKey.getN();
        return x.equals(RabinUtil.deformedMessage(s.pow(UnsignedInt.TWO, n), n));
    }
}