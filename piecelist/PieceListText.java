package piecelist;

import common.FileWriter;
import contract.PieceListContract;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class PieceListText implements PieceListContract {
    int len;            // total text length
    Piece firstPiece;   // first piece of text
    File scratch;       // scratch file for adding new stuff

    @Override
    public void loadFrom(InputStream in) {
        // TODO: read piecelisttext from file including styles and fonts
    }

    @Override
    public void storeTo(OutputStream out) {
        // TODO: write piecelisttext to file including styles and fonts
    }

    @Override
    public char charAt(int pos) {
        // TODO: return char at position pos
        final Piece p = getPieceAt(pos);

        return 0;
    }

    @Override
    public void insert(int pos, char ch) {
        // split at pos for inserting text here
        Piece p = split(pos);

        // TODO: check if not last piece on scratch file
        if (true) {
            Piece q = new Piece(0, scratch, (int) scratch.length());

            // new piece is predecessor of previous piece
            q.next = p;
            // previous p
            p.next = q;
            //
            p = q;
        }
        // write character
        FileWriter.write(scratch.getAbsolutePath(), ch);

        // increase length of piece and overall text
        p.len++;
        len++;
    }

    @Override
    public void delete(int from, int to) {
        Piece a = split(from);
        Piece b = split(to);
        // cut out all pieces in between
        a.next = b.next;
    }

    @Override
    public int indexOf(String pattern) {
        // TODO: for find method
        return 0;
    }

    @Override
    public Piece split(int pos) {
        // set p to piece containing pos
        Piece p = getPieceAt(pos);

        // split piece p
        if (pos != len) {
            int len2 = len - pos;
            int len1 = p.len - len2;
            p.len = len1;

            Piece q = new Piece(len2, p.file, p.filePos + len1);
            q.next = p.next;
            p.next = q;
        }

        return p;
    }

    private Piece getPieceAt(int pos) {
        Piece p = firstPiece;

        int len = p.len;
        while (pos > len) {
            p = p.next;
            len += p.len;
        }

        return p;
    }
}
