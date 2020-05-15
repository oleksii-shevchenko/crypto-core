package dev.flanker.rand;

import dev.flanker.alg.UnsignedInt;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

public class BbsRandomGeneratorTest {
    @Test
    @Ignore
    public void manualTest() {
        String p = "D5BBB96D30086EC484EBA3D7F9CAEB07".toLowerCase();
        String q = "425D2B9BFDB25B9CF6C416CC6E37B59C1F".toLowerCase();

        RandomGenerator generator = BbsRandomGenerator.create(UnsignedInt.valueOf(p), UnsignedInt.valueOf(q));
        System.out.println(Arrays.toString(generator.generate(16)));
    }

}