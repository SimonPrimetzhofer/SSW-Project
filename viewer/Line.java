package viewer;

public class Line {
    String text;    // text of this line
    int len;        // length of this line (including CRLF)
    int x, y, w, h; // top left corner, width, height
    int base;       // base line
    viewer.Line prev, next;
}
