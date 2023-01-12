package piecelist;

import common.UpdateEvent;
import contract.PieceListContract;
import contract.UpdateEventListener;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class PieceListText implements PieceListContract {
    private static final String SCRATCH_FILE = "./scratch.txt";
    ArrayList<UpdateEventListener> listeners = new ArrayList<>();
    private Piece firstPiece;   // first piece of text
    private File scratch;       // scratch file for adding new stuff
    private int len;            // total text length

    public PieceListText(String filename) {
        loadFrom(filename);
    }

    public int getLen() {
        return len;
    }

    private String getConcatenatedText() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char ch = charAt(i);
            stringBuilder.append(ch);
        }
        return stringBuilder.toString();
    }

    public void save() {
        try {
            String concatenatedText = getConcatenatedText();
            FileWriter fileWriter = new FileWriter(firstPiece.file);
            fileWriter.write(concatenatedText);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            System.err.println("Error opening file");
        }
    }

    @Override
    public void loadFrom(String filename) {
        try {
            FileInputStream in = new FileInputStream(filename);
            len = in.available();
            File file = new File(filename);
            firstPiece = new Piece(len, file, 0);
            in.close();
        } catch (IOException ex) {
            len = 0;
            firstPiece = new Piece(len, new File(filename), 0);
        }
        scratch = new File(SCRATCH_FILE);
        scratch.deleteOnExit();
    }

    @Override
    public char charAt(int pos) {
        if (pos < 0 || pos >= len) {
            return '\0';
        }

        Piece p = firstPiece;

        int len = p.len;
        while (pos >= len && p.next != null) {
            p = p.next;
            len += p.len;
        }

        try {
            int len2 = len - pos;
            int len1 = p.len - len2;
            FileReader reader = new FileReader(p.file);

            reader.skip(p.filePos + len1);
            int a = reader.read();
            if (a == -1) {
                return '\0';
            }
            reader.close();
            return (char) a;
        } catch (Exception ex) {
            return '\0';
        }
    }

    @Override
    public void insert(int pos, String text) {
        // split at pos for inserting text here
        Piece p = split(pos);

        if (p.file != scratch || scratch.length() != p.filePos + p.len) {
            Piece q = new Piece(0, scratch, (int) scratch.length());
            q.next = p.next;
            p.next = q;
            p = q;
        }

        // write to scratch
        try {
            FileWriter writer = new FileWriter(scratch, true);
            for (int i = 0; i < text.length(); i++) {
                writer.append(text.charAt(i));
            }
            writer.flush();
            writer.close();

            p.len += text.length();
            len += text.length();

        } catch (IOException e) {
            System.err.println("Could not write to scratch");
        }

        notify(new UpdateEvent(pos, pos, text));
    }

    @Override
    public void delete(int from, int to) {
        Piece a = split(from);
        Piece b = split(to);
        // cut out all pieces in between
        a.next = b.next;
        notify(new UpdateEvent(from, to, null));
    }

    @Override
    public int indexOf(String pattern) {
        Piece p = firstPiece;

        while (p != null) {
            int index = getFileContent(p.file, 0).indexOf(pattern);
            if (index > -1) {
                return p.filePos + index;
            }

            p = p.next;
        }

        return -1;
    }

    private String getFileContent(File file, int skip) {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            br.skip(0);
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException ex) {
            System.err.printf("Could not open or read from file %s!", file.getName());
        }

        return resultStringBuilder.toString();
    }

    @Override
    public Piece split(int pos) {
        // set p to piece containing pos
        Piece p = firstPiece;

        int len = p.len;
        while (pos >= len && p.next != null) {
            p = p.next;
            len += p.len;
        }

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

    public void setFont(int from, int to, String font) {
        Piece start = split(from).next;
        Piece end = split(to).next;

        while (start != end) {
            start.font = new Font(font, start.font.getStyle(), start.font.getSize());
            start = start.next;
        }

        // TODO: notify
    }

    public void setSize(int from, int to, int size) {
        Piece start = split(from).next;
        Piece end = split(to).next;

        while (start != end) {
            start.size = size;
            start = start.next;
        }

        // TODO: notify
    }

    public void setStyle(int from, int to, int style) {
        Piece start = split(from).next;
        Piece end = split(to).next;

        while (start != end) {
            start.style = style;
            start = start.next;
        }

        // TODO: notify
    }

    // update behaviour
    public void addUpdateEventListener(UpdateEventListener listener) {
        listeners.add(listener);
    }

    public void removeUpdateEventListener(UpdateEventListener listener) {
        listeners.remove(listener);
    }

    private void notify(UpdateEvent e) {
        Iterator<UpdateEventListener> iter = listeners.iterator();
        while (iter.hasNext()) {
            UpdateEventListener listener = iter.next();
            listener.update(e);
        }
    }
}
