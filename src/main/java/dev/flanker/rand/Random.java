package dev.flanker.rand;

public interface Random {
    void generate(byte[] bytes);

    int nextInt();

    default byte[] generate(int size) {
        byte[] bytes = new byte[size];
        generate(bytes);
        return bytes;
    }
}
