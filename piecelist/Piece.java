package piecelist;

import java.awt.*;
import java.io.File;

public class Piece {
    public int len;        // length of this piece
    public Piece next;     // next piece (obviously)
    public Font font;
    public int style;
    public int size;
    File file;      // file containing this piece
    int filePos;    // offset from beginning of file

    public Piece(int len, File file, int filePos) {
        this.len = len;
        this.file = file;
        this.filePos = filePos;
    }
}
