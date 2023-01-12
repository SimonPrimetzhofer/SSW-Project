package contract;

public interface TextContract {
    void loadFrom(String filename);

    char charAt(int pos);

    void insert(int pos, String text);

    void delete(int from, int to);

    int indexOf(String pattern);
}
