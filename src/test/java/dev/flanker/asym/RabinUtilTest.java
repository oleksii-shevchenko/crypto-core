package dev.flanker.asym;

import dev.flanker.alg.UnsignedInt;
import dev.flanker.rand.BbsRandom;
import dev.flanker.rand.Random;
import org.junit.Ignore;
import org.junit.Test;

public class RabinUtilTest {
    private static final UnsignedInt P = UnsignedInt.valueOf("D5BBB96D30086EC484EBA3D7F9CAEB07".toLowerCase());
    private static final UnsignedInt Q = UnsignedInt.valueOf("425D2B9BFDB25B9CF6C416CC6E37B59C1F".toLowerCase());

    private static final Random GENERATOR = BbsRandom.create(P, Q, UnsignedInt.random(128));

}