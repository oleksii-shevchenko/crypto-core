package dev.flanker.rand;

public interface RandomGenerator {
    void generate(byte[] bytes);

    default byte[] generate(int size) {
        byte[] bytes = new byte[size];
        generate(bytes);
        return bytes;
    }
}
