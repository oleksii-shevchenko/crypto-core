package dev.flanker.alg;

import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class UnsignedIntTest {
    private static final int ITERATIONS = 32;
    private static final int LEN = 512;

    @Test
    public void singleNumberTest() {
        assertEquals("8", UnsignedInt.valueOf(8).toString());
        assertEquals(Long.toUnsignedString(123456L, 16), UnsignedInt.valueOf(123456).toString());
    }

    @Test
    public void parsingTest() {
        String number = "ABC123453265546FBDDE231423490878979".toLowerCase();
        assertEquals(number, UnsignedInt.valueOf(number).toString());
    }

    @Test
    public void compareTest() {
        String x = "ABC123453265546FBDDE231423490878979".toLowerCase();
        String y = "23432ABC19623142349045234979".toLowerCase();

        int ui = UnsignedInt.valueOf(x).compareTo(UnsignedInt.valueOf(y));
        int bi = new BigInteger(x, 16).compareTo(new BigInteger(y, 16));

        assertEquals(ui, bi);
    }

    @Test
    public void modTest() {
        for (int i = 0; i < ITERATIONS; i++) {
            BigInteger bx = new BigInteger(LEN, ThreadLocalRandom.current());
            BigInteger bn = new BigInteger(LEN / 2, ThreadLocalRandom.current());

            UnsignedInt ux = UnsignedInt.valueOf(bx.toString(16));
            UnsignedInt un = UnsignedInt.valueOf(bn.toString(16));

            UnsignedInt ui = ux.mod(un);
            BigInteger bi = bx.mod(bn);

            assertEquals(bi.toString(16), ui.toString());
        }
    }

    @Test
    public void addTest() {
        for (int i = 0; i < ITERATIONS; i++) {
            BigInteger bx = new BigInteger(LEN, ThreadLocalRandom.current());
            BigInteger by = new BigInteger(LEN, ThreadLocalRandom.current());
            BigInteger bn = new BigInteger(LEN, ThreadLocalRandom.current());

            UnsignedInt ux = UnsignedInt.valueOf(bx.toString(16));
            UnsignedInt uy = UnsignedInt.valueOf(by.toString(16));
            UnsignedInt un = UnsignedInt.valueOf(bn.toString(16));

            UnsignedInt ui = ux.add(uy, un);
            BigInteger bi = bx.add(by).mod(bn);

            assertEquals(bi.toString(16), ui.toString());
        }
    }

   @Test
    public void multiplicationTest() {
        for (int i = 0; i < ITERATIONS; i++) {
            BigInteger bx = new BigInteger(LEN, ThreadLocalRandom.current());
            BigInteger by = new BigInteger(LEN, ThreadLocalRandom.current());
            BigInteger bn = new BigInteger(LEN, ThreadLocalRandom.current());

            UnsignedInt ux = UnsignedInt.valueOf(bx.toString(16));
            UnsignedInt uy = UnsignedInt.valueOf(by.toString(16));
            UnsignedInt un = UnsignedInt.valueOf(bn.toString(16));

            UnsignedInt ui = ux.multiply(uy, un);
            BigInteger bi = bx.multiply(by).mod(bn);

            assertEquals(bi.toString(16), ui.toString());
        }
    }

    @Test
    public void powTest() {
        for (int i = 0; i < ITERATIONS; i++) {
            System.out.println("Iteration: " + i);

            BigInteger bx = new BigInteger(LEN, ThreadLocalRandom.current());
            BigInteger be = new BigInteger(LEN, ThreadLocalRandom.current());
            BigInteger bn = new BigInteger(LEN, ThreadLocalRandom.current());

            UnsignedInt ux = UnsignedInt.valueOf(bx.toString(16));
            UnsignedInt ue = UnsignedInt.valueOf(be.toString(16));
            UnsignedInt un = UnsignedInt.valueOf(bn.toString(16));

            UnsignedInt ui = ux.power(ue, un);
            BigInteger bi = bx.modPow(be, bn);

            assertEquals(bi.toString(16), ui.toString());
        }
    }

}