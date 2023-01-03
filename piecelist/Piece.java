package piecelist;

import meta.Font;
import meta.Style;

import java.io.File;

public class Piece {
    // basic text
    int len;        // length of this piece
    File file;      // file containing this piece
    int filePos;    // offset from beginning of file
    Piece next;     // next piece (obviously)

    // meta information
    Font font;
    Style style;

    public Piece(int len, File file, int filePos) {
        this.len = len;
        this.file = file;
        this.filePos = filePos;
    }
}
