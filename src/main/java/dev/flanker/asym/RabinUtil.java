package dev.flanker.asym;

import dev.flanker.alg.UnsignedInt;
import dev.flanker.rand.MillerRabinTest;
import dev.flanker.rand.Random;

import java.util.ArrayList;
import java.util.List;

final class RabinUtil {
    private static final UnsignedInt PADDING = UnsignedInt.valueOf("ff");
    private static final int SHIFT = 64;

    private RabinUtil() { }

    static UnsignedInt generateBlumePrime(int bitLength, Random generator) {
        UnsignedInt p;
        while(true) {
            p = UnsignedInt.random(bitLength >>> 1, generator);
            p = p.multiply(UnsignedInt.valueOf(4)).add(UnsignedInt.valueOf(3));
            if (MillerRabinTest.isPrime(p)) {
                return p;
            }
        }
    }

    static int jacobiSymbol(UnsignedInt x, UnsignedInt n) {
        if (x.equals(UnsignedInt.ONE)) {
            return 1;
        }

        if (x.equals(n.subtract(UnsignedInt.ONE))) {
            if (n.getBit(1) == 1) {
                return -1;
            } else {
                return 1;
            }
        }

        if (x.equals(UnsignedInt.TWO)) {
            UnsignedInt deg = n.sqr();
            if (deg.getBit(3) == 1) {
                return -1;
            } else {
                return 1;
            }
        }

        int i = 0;
        while(!(x.getBit(0) == 1)) {
            i++;
            x = x.shiftRight(1);
        }
        
        int odd = (int) Math.pow(jacobiSymbol(UnsignedInt.TWO, n), i);
        int sign = x.subtract(UnsignedInt.ONE).multiply(n.subtract(UnsignedInt.ONE)).getBit(2) == 1 ? -1 : 1;

        return odd * sign * jacobiSymbol(n.mod(x), x);
    }

    static List<UnsignedInt> composeRoots(UnsignedInt xp, UnsignedInt p, UnsignedInt xq, UnsignedInt q) {
        UnsignedInt u = p.modInverse(q);
        UnsignedInt v = q.modInverse(p);
        UnsignedInt n = p.multiply(q);
        
        List<UnsignedInt> composedRoots = new ArrayList<>();
        
        UnsignedInt root = u.multiply(p)
                .multiply(xq)
                .add(v.multiply(q).multiply(xp))
                .mod(n);
        
        composedRoots.add(root);
        composedRoots.add(n.subtract(root));
        
        root = u.multiply(p)
                .multiply(q.subtract(xq))
                .add(v.multiply(q).multiply(xp))
                .mod(n);
        
        composedRoots.add(root);
        composedRoots.add(n.subtract(root));
        
        return composedRoots;
    }

    static UnsignedInt deformedMessage(UnsignedInt x, UnsignedInt n) {
        int byteLength = byteLength(n.bitLength());
        return x.subtract(PADDING.shiftLeft(Byte.SIZE * (byteLength - 2)))
                .shiftRight(SHIFT);
    }

    static UnsignedInt formatMessage(UnsignedInt m, UnsignedInt n, Random random) {
        int byteLength = byteLength(n.bitLength());
        return UnsignedInt.random(SHIFT, random)
                .add(m.shiftLeft(SHIFT))
                .add(PADDING.shiftLeft(Byte.SIZE * (byteLength - 2)));
    }

    private static int byteLength(int bitLength) {
        int length = bitLength / Byte.SIZE;
        length = (bitLength % Byte.SIZE == 0) ? length : length + 1;
        return length;
    }
}
