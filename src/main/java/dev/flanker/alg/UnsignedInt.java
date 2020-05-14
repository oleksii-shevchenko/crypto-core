package dev.flanker.alg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    private static final int HALF_BASE = BASE / 2;
    private static final int LENGTH = 2048;
    private static final int ARRAY_LENGTH = LENGTH / BASE;
    private static final int DOUBLE_ARRAY_LENGTH = 2 * ARRAY_LENGTH;

    private static final long BASE_MASK = (1L << BASE) - 1L;
    private static final long HALF_BASE_MASK = (1L << HALF_BASE) - 1L;

    private static final int WINDOW_LEN = 8;
    private static final long WINDOW_MASK = (1L << WINDOW_LEN) - 1L;

    private static final int RADIX = 16;
    private static final int HEXADECIMAL_BIT_LENGTH = 4;
    private static final int BASE_HEXADECIMAL_LENGTH = BASE / HEXADECIMAL_BIT_LENGTH;

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
        pow(this.arr, exponent.arr, module.arr, extendedResult);
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


    private long add(final long[] x, final long[] y, final long[] r) {
        long carry = 0;
        long temp = 0;
        for (int i = 0; i < r.length; i++) {
            temp = x[i] + y[i] + carry;
            r[i] = temp & BASE_MASK;
            carry = (temp >>> BASE) & 1;
        }
        return carry;
    }

    private void multiply(long[] x, long[] y, long[] r) {
        fill(r, 0L);
        long[] tmp = new long[r.length];
        long[] aux = new long[r.length];
        for (int i = 0; i < y.length; i++) {
            multiply(x, y[i], tmp);
            shiftBlockLeft(tmp, i);
            add(tmp, r, aux);
            arrayCopy(aux, r);
        }
    }

    private void multiply(long[] x, long y, long[] r) {
        fill(r, 0L);
        if (y == 0) {
            return;
        }
        long temp = 0;
        long overflow = 0;
        for (int i = 0; i < x.length; i++) {
            temp = x[i] * y + overflow;
            r[i] = temp & BASE_MASK;
            overflow = temp >>> BASE;
        }
        r[x.length] = overflow;
    }

    private void shiftBlockLeft(long[] x, int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        if (n == 0) {
            return;
        }
        for (int i = x.length - 1; i >= 0; i--) {
            if (i - n >= 0) {
                x[i] = x[i - n];
            } else {
                x[i] = 0L;
            }
        }
    }

    private void shiftBitLeft(long[] x, int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        if (n == 0) {
            return;
        }
        for (int i = BASE * x.length - 1; i >= 0; i--) {
            if (i - n >= 0) {
                setBit(x, getBit(x, i - n), i);
            } else {
                setBit(x, 0, i);
            }
        }
    }

    private long getBit(long[] x, int position) {
        return (x[position / BASE] >>> (position % BASE)) & 1;
    }

    private void setBit(long[] x, long bit, int position) {
        if (bit == 0) {
            x[position / BASE] = (x[position / BASE] & (~(1L << (position % BASE)))) & BASE_MASK;
        } else if (bit == 1) {
            x[position / BASE] = x[position / BASE] | (1L << (position % BASE));
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void subtract(long[] x, long[] y, long[] r) {
        long borrow = 0;
        long temp = 0;
        for (int i = 0; i < r.length; i++) {
            temp = x[i] - y[i] - borrow;
            if (temp >= 0) {
                r[i] = temp;
                borrow = 0;
            } else {
                r[i] = temp + (1L << BASE);
                borrow = 1;
            }
        }
    }

    private void inplaceMod(long[] x, long[] n) {
        if (x.length != n.length) {
            throw new IllegalArgumentException();
        }
        long[] tmp = new long[n.length];
        long[] aux = new long[n.length];
        int highestN = highestNonZeroBit(n);
        while (compare(x, n) != -1) {
            arrayCopy(n, aux);
            int shift = highestNonZeroBit(x) - highestN;
            shiftBitLeft(aux, shift);
            if (compare(x, aux) == -1) {
                arrayCopy(n, aux);
                shiftBitLeft(aux, --shift);
            }
            subtract(x, aux, tmp);
            arrayCopy(tmp, x);
        }
    }

    private int highestNonZeroBit(long[] x) {
        int i, j;
        for (i = x.length - 1; i > -1; i--)
            if (x[i] != 0)
                break;
        for (j = BASE - 1; j > -1; j--)
            if (((x[i] >>> j) & 1) != 0)
                break;
        return (BASE * i + j);
    }

    private int highestNonZeroBlock(long[] x) {
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

    private int compare(long[] x, long[] y) {
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

    private void pow(long[] x, long[] e, long[] n, long[] r) {
        uniteArray(r);

        long[] expandedN = new long[DOUBLE_ARRAY_LENGTH];
        long[] temp = new long[DOUBLE_ARRAY_LENGTH];
        long[] smallTemp = new long[ARRAY_LENGTH];

        arraycopy(n, 0, expandedN, 0, ARRAY_LENGTH);

        long[][] powers = new long[pow(2, WINDOW_LEN)][ARRAY_LENGTH];
        uniteArray(powers[0]);
        arrayCopy(x, powers[1]);
        for (int i = 2; i < powers.length; i++) {
            multiply(x, powers[i - 1], temp);
            inplaceMod(temp, expandedN);
            arraycopy(temp, 0, powers[i], 0, ARRAY_LENGTH);
            fill(temp, 0);
        }

        for (int i = highestNonZeroBlock(e); i > -1; i--) {
            for (int j = BASE / WINDOW_LEN - 1; j > -1; j--) {
                int index = (int) ((e[i] >>> (WINDOW_LEN * j)) & WINDOW_MASK);
                arraycopy(r, 0, smallTemp, 0, ARRAY_LENGTH);
                multiply(smallTemp, powers[index], r);
                inplaceMod(r, expandedN);
                if (i + j != 0) {
                    for (int k = 0; k < WINDOW_LEN; k++) {
                        arraycopy(r, 0, smallTemp, 0, ARRAY_LENGTH);
                        multiply(smallTemp, smallTemp, r);
                        inplaceMod(r, expandedN);
                    }
                }
            }
        }
    }

    private int pow(int x, int e) {
        return (int) Math.pow(x, e);
    }

    private void uniteArray(long[] x) {
        fill(x, 0);
        x[0] = 1;
    }

    private void arrayCopy(long[] src, long[] dst) {
        arraycopy(src, 0, dst, 0, dst.length);
    }


    @Override
    public int compareTo(UnsignedInt o) {
        return compare(this.arr, o.arr);
    }
}
