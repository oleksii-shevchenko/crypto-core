package dev.flanker.alg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;

public class UnsignedInt implements Comparable<UnsignedInt> {
    private static final Map<Character, Long> CODES = new HashMap<>();

    public static final UnsignedInt ZERO = new UnsignedInt(0);
    public static final UnsignedInt ONE = new UnsignedInt(1);
    public static final UnsignedInt TWO = new UnsignedInt(2);

    private static final Map<Long, UnsignedInt> CONSTANT_CACHE = Map.of(
            0L, ZERO,
            1L, ONE,
            2L, TWO
    );

    private static final int BASE = 32;
    private static final int LENGTH = 2048;
    private static final int ARRAY_LENGTH = LENGTH / BASE;
    private static final int DOUBLE_ARRAY_LENGTH = 2 * ARRAY_LENGTH;

    private static final long BASE_MASK = (1L << BASE) - 1L;

    private static final int WINDOW_LEN = 8;
    private static final long WINDOW_MASK = (1L << WINDOW_LEN) - 1L;
    private static final int PRECOMPUTED_CACHE_SIZE = pow(2, WINDOW_LEN);

    private static final int RADIX = 16;
    private static final int HEXADECIMAL_BIT_LENGTH = 4;
    private static final int BASE_HEXADECIMAL_LENGTH = BASE / HEXADECIMAL_BIT_LENGTH;
    private static final int BASE_BYTE_LENGTH = BASE / Byte.SIZE;

    private final long[] arr;

    static {
        // Codes initialization
        CODES.put('0', 0L);
        CODES.put('1', 1L);
        CODES.put('2', 2L);
        CODES.put('3', 3L);
        CODES.put('4', 4L);
        CODES.put('5', 5L);
        CODES.put('6', 6L);
        CODES.put('7', 7L);
        CODES.put('8', 8L);
        CODES.put('9', 9L);
        CODES.put('a', 10L);
        CODES.put('b', 11L);
        CODES.put('c', 12L);
        CODES.put('d', 13L);
        CODES.put('e', 14L);
        CODES.put('f', 15L);
    }

    private UnsignedInt() {
        this.arr = new long[ARRAY_LENGTH];
    }

    private UnsignedInt(long[] arr) {
        this.arr = arr;
    }

    private UnsignedInt(long number) {
        this.arr = new long[ARRAY_LENGTH];
        this.arr[0] = number;
    }

    public static UnsignedInt valueOf(int number) {
        if (CONSTANT_CACHE.containsKey((long) number)) {
            return CONSTANT_CACHE.get((long) number);
        }
        return new UnsignedInt(number);
    }

    public static UnsignedInt valueOf(String number) {
        try {
            long[] arr = new long[ARRAY_LENGTH];
            int shift = 0;
            int block = 0;
            for (int i = number.length() - 1; i > -1; i--) {
                arr[block] ^= CODES.get(number.charAt(i)) << (HEXADECIMAL_BIT_LENGTH * shift);

                shift++;
                block += (shift / BASE_HEXADECIMAL_LENGTH);
                shift = shift % BASE_HEXADECIMAL_LENGTH;
            }
            return new UnsignedInt(arr);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static UnsignedInt valueOf(UnsignedInt number) {
        long[] arr = new long[ARRAY_LENGTH];
        arrayCopy(number.arr, arr);
        return new UnsignedInt(arr);
    }

    public static UnsignedInt valueOf(byte[] bytes) {
        if (bytes.length != (LENGTH / Byte.SIZE)) {
            throw new IllegalArgumentException();
        }
        try {
            long[] arr = new long[ARRAY_LENGTH];
            int shift = 0;
            int block = 0;
            for (int i = 0; i < bytes.length; i++) {
                arr[block] ^=  Byte.toUnsignedLong(bytes[i]) << (Byte.SIZE * shift);

                shift++;
                block += (shift / BASE_BYTE_LENGTH);
                shift = shift % BASE_BYTE_LENGTH;
            }
            return new UnsignedInt(arr);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static UnsignedInt random(int bits) {
        UnsignedInt module = new UnsignedInt();
        setBit(module.arr, 1, bits);

        long[] result = new long[ARRAY_LENGTH];
        int blocks = min(bits / BASE + 1, ARRAY_LENGTH);
        for (int i = 0; i < blocks; i++) {
            result[i] = ThreadLocalRandom.current().nextLong() & BASE_MASK;
        }
        inplaceMod(result, module.arr);
        return new UnsignedInt(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnsignedInt that = (UnsignedInt) o;
        return Arrays.equals(arr, that.arr);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arr);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = highestNonZeroBlock(arr); i > -1; i--) {
            for (int j = BASE / HEXADECIMAL_BIT_LENGTH - 1; j > -1; j--) {
                long chunk = (arr[i] >> (4 * j)) & 0xF;
                builder.append(Long.toUnsignedString(chunk, RADIX));
            }
        }
        while ((builder.length() > 1) && (builder.charAt(0) == '0')) {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    public UnsignedInt add(UnsignedInt that, UnsignedInt module) {
        long[] result = new long[ARRAY_LENGTH];
        long carry = add(this.arr, that.arr, result);
        if (carry == 0) {
            inplaceMod(result, module.arr);
            return new UnsignedInt(result);
        } else {
            long[] extendedModule = new long[DOUBLE_ARRAY_LENGTH];
            long[] extendedResult = new long[DOUBLE_ARRAY_LENGTH];
            arraycopy(module.arr, 0, extendedModule, 0, ARRAY_LENGTH);
            arraycopy(result, 0, extendedResult, 0, ARRAY_LENGTH);
            extendedResult[ARRAY_LENGTH] = 1;
            inplaceMod(extendedResult, extendedModule);
            arraycopy(extendedResult, 0, result, 0, ARRAY_LENGTH);
            return new UnsignedInt(result);
        }
    }

    public UnsignedInt multiply(UnsignedInt that) {
        long[] extendedResult = new long[DOUBLE_ARRAY_LENGTH];
        multiply(this.arr, that.arr, extendedResult);
        long[] result = new long[ARRAY_LENGTH];
        arraycopy(extendedResult, 0, result, 0, ARRAY_LENGTH);
        return new UnsignedInt(result);
    }

    public UnsignedInt multiply(UnsignedInt that, UnsignedInt module) {
        long[] extendedResult = new long[DOUBLE_ARRAY_LENGTH];
        long[] extendedModule = new long[DOUBLE_ARRAY_LENGTH];
        arraycopy(module.arr, 0, extendedModule, 0, ARRAY_LENGTH);
        multiply(this.arr, that.arr, extendedResult);
        inplaceMod(extendedResult, extendedModule);
        long[] result = new long[ARRAY_LENGTH];
        arraycopy(extendedResult, 0, result, 0, ARRAY_LENGTH);
        return new UnsignedInt(result);
    }

    public UnsignedInt power(UnsignedInt exponent, UnsignedInt module) {
        long[] extendedResult = new long[DOUBLE_ARRAY_LENGTH];
        windowPow(this.arr, exponent.arr, module.arr, extendedResult);
        long[] result = new long[ARRAY_LENGTH];
        arraycopy(extendedResult, 0, result, 0, ARRAY_LENGTH);
        return new UnsignedInt(result);
    }

    public UnsignedInt mod(UnsignedInt module) {
        long[] result = new long[ARRAY_LENGTH];
        arraycopy(this.arr, 0, result, 0, ARRAY_LENGTH);
        inplaceMod(result, module.arr);
        return new UnsignedInt(result);
    }

    public UnsignedInt sqr(UnsignedInt module) {
        return multiply(this, module);
    }

    public UnsignedInt shiftLeft(int bits) {
        long[] result = new long[ARRAY_LENGTH];
        arraycopy(this.arr, 0, result, 0, ARRAY_LENGTH);
        shiftBitLeft(result, bits);
        return new UnsignedInt(result);
    }

    public UnsignedInt shiftRight(int bits) {
        long[] result = new long[ARRAY_LENGTH];
        arraycopy(this.arr, 0, result, 0, ARRAY_LENGTH);
        shiftBitRight(result, bits);
        return new UnsignedInt(result);
    }

    public int bitLength() {
        return highestNonZeroBit(this.arr);
    }

    public int getBit(int position) {
        return (int) getBit(this.arr, position);
    }

    private static long add(final long[] x, final long[] y, final long[] r) {
        long carry = 0;
        long temp = 0;
        for (int i = 0; i < r.length; i++) {
            temp = x[i] + y[i] + carry;
            r[i] = temp & BASE_MASK;
            carry = (temp >>> BASE) & 1;
        }
        return carry;
    }

    private static void multiply(long[] first, long[] second, long[] result) {
        multiply(first, second, result, new long[result.length], new long[result.length]);
    }

    private static void multiply(long[] first, long[] second, long[] result, long[] tempBuffer, long[] auxBuffer) {
        fill(result, 0L);
        for (int i = 0; i < second.length; i++) {
            multiply(first, second[i], tempBuffer);
            shiftBlockLeft(tempBuffer, i);
            add(tempBuffer, result, auxBuffer);
            arrayCopy(auxBuffer, result);
        }
    }

    private static void multiply(long[] first, long second, long[] result) {
        if (second == 0) {
            fill(result, 0L);
            return;
        }

        long temp = 0;
        long overflow = 0;
        for (int i = 0; i < first.length; i++) {
            temp = first[i] * second + overflow;
            result[i] = temp & BASE_MASK;
            overflow = temp >>> BASE;
        }

        result[first.length] = overflow;
        fill(result, first.length + 1, result.length, 0L);

    }

    private static void shiftBlockLeft(long[] number, int blocks) {
        if (blocks < 0) {
            throw new IllegalArgumentException();
        }
        if (blocks == 0) {
            return;
        }
        for (int i = number.length - 1; i >= 0; i--) {
            if (i - blocks >= 0) {
                number[i] = number[i - blocks];
            } else {
                number[i] = 0L;
            }
        }
    }

    private static void shiftBitLeft(long[] number, int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException();
        }
        if (bits == 0) {
            return;
        }
        for (int i = BASE * number.length - 1; i >= 0; i--) {
            if (i - bits >= 0) {
                setBit(number, getBit(number, i - bits), i);
            } else {
                setBit(number, 0, i);
            }
        }
    }

    private static void shiftBitRight(long[] number, int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException();
        }
        if (bits == 0) {
            return;
        }
        int lim = BASE * number.length;
        for (int i = 0; i < lim; i++) {
            if (i + bits < lim) {
                setBit(number, getBit(number, i + bits), i);
            } else {
                setBit(number, 0, i);
            }
        }
    }

    private static long getBit(long[] x, int position) {
        return (x[position / BASE] >>> (position % BASE)) & 1;
    }

    private static void setBit(long[] x, long bit, int position) {
        if (bit == 0) {
            x[position / BASE] = (x[position / BASE] & (~(1L << (position % BASE)))) & BASE_MASK;
        } else if (bit == 1) {
            x[position / BASE] = x[position / BASE] | (1L << (position % BASE));
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static void subtract(long[] first, long[] second, long[] result) {
        long borrow = 0;
        long temp = 0;
        for (int i = 0; i < result.length; i++) {
            temp = first[i] - second[i] - borrow;
            if (temp >= 0) {
                result[i] = temp;
                borrow = 0;
            } else {
                result[i] = temp + (1L << BASE);
                borrow = 1;
            }
        }
    }

    private static void inplaceMod(long[] number, long[] module) {
        inplaceMod(number, module, new long[module.length], new long[module.length]);
    }

    private static void inplaceMod(long[] number, long[] module, long[] tempBuffer, long[] auxBuffer) {
        if (number.length != module.length) {
            throw new IllegalArgumentException();
        }
        int highestN = highestNonZeroBit(module);
        while (compare(number, module) > 0) {
            arrayCopy(module, auxBuffer);
            int shift = highestNonZeroBit(number) - highestN;
            shiftBitLeft(auxBuffer, shift);
            if (compare(number, auxBuffer) < 0) {
                arrayCopy(module, auxBuffer);
                shiftBitLeft(auxBuffer, --shift);
            }
            subtract(number, auxBuffer, tempBuffer);
            arrayCopy(tempBuffer, number);
        }
    }

    private static int highestNonZeroBit(long[] x) {
        int i, j;
        for (i = x.length - 1; i > -1; i--)
            if (x[i] != 0)
                break;
        for (j = BASE - 1; j > -1; j--)
            if (((x[i] >>> j) & 1) != 0)
                break;
        return (BASE * i + j);
    }

    private static int highestNonZeroBlock(long[] x) {
        int i = x.length - 1;
        while (i > -1) {
            if (x[i] == 0) {
                i--;
            } else {
                return i;
            }
        }
        return 0;
    }

    private static int compare(long[] x, long[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException();
        }
        int position = x.length - 1;
        while (position > -1 && x[position] == y[position]) {
            position--;
        }
        if (position == -1) {
            return 0;
        } else {
            return Long.compareUnsigned(x[position], y[position]);
        }
    }

    private static void windowPow(long[] x, long[] e, long[] n, long[] r) {
        uniteArray(r);

        long[] firstBuffer = new long[DOUBLE_ARRAY_LENGTH];
        long[] secondBuffer = new long[DOUBLE_ARRAY_LENGTH];
        long[] en = new long[DOUBLE_ARRAY_LENGTH];
        long[] temp = new long[DOUBLE_ARRAY_LENGTH];
        long[] smallTemp = new long[ARRAY_LENGTH];

        arraycopy(n, 0, en, 0, ARRAY_LENGTH);

        long[][] powers = precomputedPowers(x, en, temp, firstBuffer, secondBuffer);

        for (int i = highestNonZeroBlock(e); i > -1; i--) {
            for (int j = BASE / WINDOW_LEN - 1; j > -1; j--) {
                int index = getPrecomputedIndex(e[i], j);
                arraycopy(r, 0, smallTemp, 0, ARRAY_LENGTH);
                multiply(smallTemp, powers[index], r, firstBuffer, secondBuffer);
                inplaceMod(r, en);
                if (i + j != 0) {
                    for (int k = 0; k < WINDOW_LEN; k++) {
                        arraycopy(r, 0, smallTemp, 0, ARRAY_LENGTH);
                        multiply(smallTemp, smallTemp, r, firstBuffer, secondBuffer);
                        inplaceMod(r, en, firstBuffer, secondBuffer);
                    }
                }
            }
        }
    }

    private static int getPrecomputedIndex(long block, int shift) {
        return (int) ((block >>> (WINDOW_LEN * shift)) & WINDOW_MASK);
    }

    private static long[][] precomputedPowers(long[] x, long[] n, long[] temp, long[] firstBuffer, long[] secondBuffer) {
        long[][] powers = new long[PRECOMPUTED_CACHE_SIZE][ARRAY_LENGTH];
        uniteArray(powers[0]);
        arrayCopy(x, powers[1]);
        for (int i = 2; i < powers.length; i++) {
            multiply(x, powers[i - 1], temp, firstBuffer, secondBuffer);
            inplaceMod(temp, n, firstBuffer, secondBuffer);
            arraycopy(temp, 0, powers[i], 0, ARRAY_LENGTH);
            fill(temp, 0);
        }
        return powers;
    }

    private static int pow(int x, int e) {
        return (int) Math.pow(x, e);
    }

    private static void uniteArray(long[] x) {
        fill(x, 0);
        x[0] = 1;
    }

    private static void arrayCopy(long[] src, long[] dst) {
        arraycopy(src, 0, dst, 0, dst.length);
    }


    @Override
    public int compareTo(UnsignedInt o) {
        return compare(this.arr, o.arr);
    }
}
