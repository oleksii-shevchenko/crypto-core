package dev.flanker.alg;

import dev.flanker.rand.Random;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Integer.lowestOneBit;
import static java.lang.Integer.toUnsignedLong;
import static java.util.Arrays.fill;

public class UnsignedInt implements Comparable<UnsignedInt> {
    private static final Map<Character, Integer> CODES = new HashMap<>();

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

    private static final int WINDOW_LEN = 4;
    private static final int WINDOW_MASK = (1 << WINDOW_LEN) - 1;
    private static final int PRECOMPUTED_CACHE_SIZE = (int) Math.pow(2, WINDOW_LEN);

    private static final int RADIX = 16;
    private static final int HEXADECIMAL_BIT_LENGTH = 4;
    private static final int BASE_HEXADECIMAL_LENGTH = BASE / HEXADECIMAL_BIT_LENGTH;
    private static final int BASE_BYTE_LENGTH = BASE / Byte.SIZE;

    private final int[] digits;

    static {
        // Codes initialization
        CODES.put('0', 0);
        CODES.put('1', 1);
        CODES.put('2', 2);
        CODES.put('3', 3);
        CODES.put('4', 4);
        CODES.put('5', 5);
        CODES.put('6', 6);
        CODES.put('7', 7);
        CODES.put('8', 8);
        CODES.put('9', 9);
        CODES.put('a', 10);
        CODES.put('b', 11);
        CODES.put('c', 12);
        CODES.put('d', 13);
        CODES.put('e', 14);
        CODES.put('f', 15);
    }

    private UnsignedInt() {
        this(new int[DOUBLE_ARRAY_LENGTH]);
    }

    public UnsignedInt(int digit) {
        this.digits = new int[DOUBLE_ARRAY_LENGTH];
        this.digits[0] = digit;
    }

    private UnsignedInt(int[] digits) {
        assert digits.length == DOUBLE_ARRAY_LENGTH;
        this.digits = digits;
    }


    // <======================= Constructors ======================>
    public static UnsignedInt valueOf(int number) {
        if (CONSTANT_CACHE.containsKey((long) number)) {
            return CONSTANT_CACHE.get((long) number);
        }
        return new UnsignedInt(number);
    }

    public static UnsignedInt valueOf(String number) {
        try {
            int[] digits = new int[DOUBLE_ARRAY_LENGTH];
            int shift = 0;
            int block = 0;
            for (int i = number.length() - 1; i > -1; i--) {
                digits[block] ^= CODES.get(number.charAt(i)) << (HEXADECIMAL_BIT_LENGTH * shift);

                shift++;
                block += (shift / BASE_HEXADECIMAL_LENGTH);
                shift = shift % BASE_HEXADECIMAL_LENGTH;
            }
            return new UnsignedInt(digits);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static UnsignedInt valueOf(UnsignedInt number) {
        int[] digits = new int[DOUBLE_ARRAY_LENGTH];
        arrayCopy(number.digits, digits);
        return new UnsignedInt(digits);
    }

    public static UnsignedInt random(int bits) {
        int[] digits = new int[DOUBLE_ARRAY_LENGTH];
        int blocks = bits / BASE;
        for (int i = 0; i < blocks; i++) {
            digits[i] = ThreadLocalRandom.current().nextInt();
        }
        digits[blocks] = ThreadLocalRandom.current().nextInt() & ((1 << (bits / BASE)) - 1);
        return new UnsignedInt(digits);
    }

    public static UnsignedInt random(int bits, Random random) {
        int[] digits = new int[DOUBLE_ARRAY_LENGTH];
        int blocks = bits / BASE;
        for (int i = 0; i < blocks; i++) {
            digits[i] = random.nextInt();
        }
        digits[blocks] = random.nextInt() & ((1 << (bits / BASE)) - 1);
        return new UnsignedInt(digits);
    }


    // <======================= Basic API =======================>
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = highestNonZeroBlock(digits); i > -1; i--) {
            for (int j = BASE / HEXADECIMAL_BIT_LENGTH - 1; j > -1; j--) {
                int chunk = (digits[i] >> (HEXADECIMAL_BIT_LENGTH * j)) & 0xF;
                builder.append(Integer.toUnsignedString(chunk, RADIX));
            }
        }
        while ((builder.length() > 1) && (builder.charAt(0) == '0')) {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnsignedInt unsignedInt = (UnsignedInt) o;
        return compare(this.digits, unsignedInt.digits, ARRAY_LENGTH) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < ARRAY_LENGTH; i++) {
            hash ^= digits[i];
        }
        return hash;
    }

    @Override
    public int compareTo(UnsignedInt o) {
        return compare(this.digits, o.digits, ARRAY_LENGTH);
    }


    // <======================= Public API =======================>
    public UnsignedInt add(UnsignedInt that) {
        int[] result = new int[DOUBLE_ARRAY_LENGTH];
        add(this.digits, that.digits, result);
        fill(result, ARRAY_LENGTH, DOUBLE_ARRAY_LENGTH, 0);
        return new UnsignedInt(result);
    }

    public UnsignedInt add(UnsignedInt that, UnsignedInt module) {
        int[] result = new int[DOUBLE_ARRAY_LENGTH];
        add(this.digits, that.digits, result);
        inplaceMod(result, module.digits, new int[DOUBLE_ARRAY_LENGTH], new int[DOUBLE_ARRAY_LENGTH]);
        return new UnsignedInt(result);
    }

    public UnsignedInt subtract(UnsignedInt that) {
        int[] result = new int[DOUBLE_ARRAY_LENGTH];
        subtract(this.digits, that.digits, result);
        fill(result, ARRAY_LENGTH, DOUBLE_ARRAY_LENGTH, 0);
        return new UnsignedInt(result);
    }

    public UnsignedInt multiply(UnsignedInt that) {
        int[] result = new int[DOUBLE_ARRAY_LENGTH];
        multiply(this.digits, that.digits, result, new int[DOUBLE_ARRAY_LENGTH], new int[DOUBLE_ARRAY_LENGTH]);
        fill(result, ARRAY_LENGTH, DOUBLE_ARRAY_LENGTH, 0);
        return new UnsignedInt(result);
    }

    public UnsignedInt multiply(UnsignedInt that, UnsignedInt module) {
        int[] result = new int[DOUBLE_ARRAY_LENGTH];
        int[] firstBuffer = new int[DOUBLE_ARRAY_LENGTH];
        int[] secondBuffer = new int[DOUBLE_ARRAY_LENGTH];
        multiply(this.digits, that.digits, result, firstBuffer, secondBuffer);
        inplaceMod(result, module.digits, firstBuffer, secondBuffer);
        return new UnsignedInt(result);
    }

    public UnsignedInt mod(UnsignedInt module) {
        int[] result = new int[DOUBLE_ARRAY_LENGTH];
        arrayCopy(this.digits, result);
        inplaceMod(result, module.digits, new int[DOUBLE_ARRAY_LENGTH], new int[DOUBLE_ARRAY_LENGTH]);
        return new UnsignedInt(result);
    }

    public UnsignedInt modInverse(UnsignedInt m) {
        return null;
    }

    public UnsignedInt pow(UnsignedInt exponent, UnsignedInt module) {
        int[] result = new int[DOUBLE_ARRAY_LENGTH];
        windowPow(this.digits,
                exponent.digits,
                module.digits,
                result,
                new int[DOUBLE_ARRAY_LENGTH],
                new int[DOUBLE_ARRAY_LENGTH],
                new int[DOUBLE_ARRAY_LENGTH]
        );
        return new UnsignedInt(result);
    }

    public UnsignedInt sqr() {
        return multiply(this);
    }

    public UnsignedInt sqr(UnsignedInt module) {
        return multiply(this, module);
    }

    public UnsignedInt shiftRight(int bits) {
        int[] result = new int[DOUBLE_ARRAY_LENGTH];
        arrayCopy(digits, result);
        shiftBitRight(result, bits);
        fill(result, ARRAY_LENGTH, DOUBLE_ARRAY_LENGTH, 0);
        return new UnsignedInt(result);
    }

    public UnsignedInt shiftLeft(int bits) {
        int[] result = new int[DOUBLE_ARRAY_LENGTH];
        arrayCopy(digits, result);
        shiftBitLeft(result, bits);
        fill(result, ARRAY_LENGTH, DOUBLE_ARRAY_LENGTH, 0);
        return new UnsignedInt(result);
    }

    public int getBit(int position) {
        return getBit(digits, position);
    }

    public UnsignedInt setBit(int bit, int position) {
        int[] result = new int[DOUBLE_ARRAY_LENGTH];
        arrayCopy(digits, result);
        setBit(result, bit, position);
        fill(result, ARRAY_LENGTH, DOUBLE_ARRAY_LENGTH, 0);
        return new UnsignedInt(result);
    }

    public int bitLength() {
        return highestNonZeroBit(digits);
    }

    // <=================== Arithmetic Operators ===================>

    private static void add(final int[] x, final int[] y, final int[] result) {
        long carry = 0;
        long temp = 0;
        for (int i = 0; i < DOUBLE_ARRAY_LENGTH; i++) {
            temp = toUnsignedLong(x[i]) + toUnsignedLong(y[i]) + carry;
            result[i] = (int) temp;
            carry = (temp >>> BASE) & 1;
        }
    }

    private static void subtract(int[] x, int[] y, int[] result) {
        long borrow = 0;
        long temp = 0;
        for (int i = 0; i < DOUBLE_ARRAY_LENGTH; i++) {
            temp = toUnsignedLong(x[i]) - toUnsignedLong(y[i]) - borrow;
            if (temp >= 0) {
                result[i] = (int) temp;
                borrow = 0;
            } else {
                result[i] = (int) (temp + (1L << BASE));
                borrow = 1;
            }
        }
    }

    private static void multiply(int[] x, int y, int[] result) {
        if (y == 0) {
            fill(result, 0);
            return;
        }

        long temp = 0;
        long overflow = 0;
        for (int i = 0; i < ARRAY_LENGTH; i++) {
            temp = toUnsignedLong(x[i]) * toUnsignedLong(y) + overflow;
            result[i] = (int) temp;
            overflow = temp >>> BASE;
        }

        result[ARRAY_LENGTH] = (int) overflow;
        fill(result, ARRAY_LENGTH + 1, DOUBLE_ARRAY_LENGTH, 0);
    }

    private static void multiply(int[] x, int[] y, int[] result, int[] multBuffer, int[] addBuffer) {
        fill(multBuffer, 0);
        fill(addBuffer, 0);
        fill(result, 0);

        for (int i = 0; i < ARRAY_LENGTH; i++) {
            multiply(x, y[i], multBuffer);
            shiftBlockLeft(multBuffer, i);
            add(multBuffer, result, addBuffer);
            arrayCopy(addBuffer, result);
        }
    }

    private static void windowPow(int[] x, int[] e, int[] m, int[] r, int[] temp, int[] firstBuffer, int[] secondBuffer) {
        fill(temp, 0);
        fill(firstBuffer, 0);
        fill(secondBuffer, 0);

        uniteArray(r);

        int[][] powers = precomputedPowers(x, m, firstBuffer, secondBuffer);

        for (int i = highestNonZeroBlock(e); i > -1; i--) {
            for (int j = BASE / WINDOW_LEN - 1; j > -1; j--) {
                int index = getPrecomputedIndex(e[i], j);
                arrayCopy(r, temp);
                multiply(temp, powers[index], r, firstBuffer, secondBuffer);
                inplaceMod(r, m, firstBuffer, secondBuffer);

                if (i + j != 0) {
                    for (int k = 0; k < WINDOW_LEN; k++) {
                        arrayCopy(r, temp);
                        multiply(temp, temp, r, firstBuffer, secondBuffer);
                        inplaceMod(r, m, firstBuffer, secondBuffer);
                    }
                }
            }
        }
    }

    private static int getPrecomputedIndex(int block, int shift) {
        return (block >>> (WINDOW_LEN * shift)) & WINDOW_MASK;
    }

    private static int[][] precomputedPowers(int[] x, int[] n, int[] firstBuffer, int[] secondBuffer) {
        fill(firstBuffer, 0);
        fill(secondBuffer, 0);

        int[][] powers = new int[PRECOMPUTED_CACHE_SIZE][DOUBLE_ARRAY_LENGTH];
        uniteArray(powers[0]);
        arrayCopy(x, powers[1]);
        for (int i = 2; i < PRECOMPUTED_CACHE_SIZE; i++) {
            multiply(x, powers[i - 1], powers[i], firstBuffer, secondBuffer);
            inplaceMod(powers[i], n, firstBuffer, secondBuffer);
        }
        return powers;
    }


    // <==================== Modular Operators ====================>

    private static void inplaceMod(int[] x, int[] m, int[] subtractBuffer, int[] shiftBuffer) {
        fill(shiftBuffer, 0);
        fill(subtractBuffer, 0);

        int highestM = highestNonZeroBit(m);
        while (compare(x, m, DOUBLE_ARRAY_LENGTH) > 0) {
            arrayCopy(m, shiftBuffer);
            int bits = highestNonZeroBit(x) - highestM;
            shiftBitLeft(shiftBuffer, bits);

            if (compare(x, shiftBuffer, DOUBLE_ARRAY_LENGTH) < 0) {
                arrayCopy(m, shiftBuffer);
                shiftBitLeft(shiftBuffer, --bits);
            }

            subtract(x, shiftBuffer, subtractBuffer);
            arrayCopy(subtractBuffer, x);
        }
    }


    // <===================== Bit Operators =====================>

    private static void shiftBlockLeft(int[] x, int blocks) {
        if (blocks < 0) {
            throw new IllegalArgumentException();
        }

        if (blocks == 0) {
            return;
        }

        for (int i = DOUBLE_ARRAY_LENGTH - 1; i >= 0; i--) {
            if (i - blocks >= 0) {
                x[i] = x[i - blocks];
            } else {
                x[i] = 0;
            }
        }
    }

    private static void shiftBitLeft(int[] x, int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException();
        }

        if (bits == 0) {
            return;
        }

        if (bits % BASE == 0) {
            shiftBlockLeft(x, bits / BASE);
            return;
        }

        int blockShift = bits / BASE;
        int bitShift = bits % BASE;
        for (int i = DOUBLE_ARRAY_LENGTH - 1; i >= 0; i--) {
            int firstBlock = i - blockShift >= 0 ? x[i - blockShift] : 0;
            int secondBlock = i - blockShift - 1 >= 0 ? x[i - blockShift - 1] : 0;
            x[i] = (firstBlock << bitShift) | (secondBlock >>> (BASE - bitShift));
        }
    }



    private static void shiftBlockRight(int[] x, int blocks) {
        if (blocks < 0) {
            throw new IllegalArgumentException();
        }

        if (blocks == 0) {
            return;
        }

        for (int i = 0; i < DOUBLE_ARRAY_LENGTH; i++) {
            if (i + blocks < DOUBLE_ARRAY_LENGTH) {
                x[i] = x[i + blocks];
            } else {
                x[i] = 0;
            }
        }
    }

    private static void shiftBitRight(int[] x, int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException();
        }

        if (bits == 0) {
            return;
        }

        if (bits % BASE == 0) {
            shiftBlockRight(x, bits / BASE);
            return;
        }

        int blockShift = bits / BASE;
        int bitShift = bits % BASE;
        for (int i = 0; i < DOUBLE_ARRAY_LENGTH; i++) {
            int firstBlock = i + blockShift < DOUBLE_ARRAY_LENGTH ? x[i + blockShift] : 0;
            int secondBlock = i + blockShift + 1 < DOUBLE_ARRAY_LENGTH ? x[i + blockShift + 1] : 0;
            x[i] = (firstBlock >>> bitShift) | (secondBlock << (BASE - bitShift));
        }
    }

    private static int getBit(int[] x, int position) {
        return (x[position / BASE] >>> (position % BASE)) & 1;
    }

    private static void setBit(int[] x, int bit, int position) {
        switch (bit) {
            case 0:
                x[position / BASE] = x[position / BASE] & (~(1 << (position % BASE)));
                break;
            case 1:
                x[position / BASE] = x[position / BASE] | (1 << (position % BASE));
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static int highestNonZeroBlock(int[] x) {
        int i = DOUBLE_ARRAY_LENGTH - 1;
        while (i > -1) {
            if (x[i] == 0) {
                i--;
            } else {
                return i;
            }
        }
        return 0;
    }

    private static int highestNonZeroBit(int[] x) {
        int i, j;
        for (i = DOUBLE_ARRAY_LENGTH - 1; i > -1; i--)
            if (x[i] != 0)
                break;
        for (j = BASE - 1; j > -1; j--)
            if (((x[i] >>> j) & 1) != 0)
                break;
        return (BASE * i + j);
    }


    // <===================== Util Operators =====================>

    private static void arrayCopy(int[] src, int[] dst) {
        System.arraycopy(src, 0, dst, 0, DOUBLE_ARRAY_LENGTH);
    }

    private static void uniteArray(int[] x) {
        fill(x, 0);
        x[0] = 1;
    }

    private static int compare(int[] x, int[] y, int precision) {
        int position = precision - 1;
        while (position > -1 && x[position] == y[position]) {
            position--;
        }
        if (position == -1) {
            return 0;
        } else {
            return Integer.compareUnsigned(x[position], y[position]);
        }
    }
}
