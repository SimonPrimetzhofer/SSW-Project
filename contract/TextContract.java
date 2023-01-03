package contract;

import java.io.InputStream;
import java.io.OutputStream;

public interface TextContract {
    void loadFrom(InputStream in);
    void storeTo(OutputStream out);

    char charAt(int pos);

    void insert(int pos, char ch);
    void delete(int from, int to);

    int indexOf(String pattern);
}
