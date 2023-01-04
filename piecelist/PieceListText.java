package piecelist;

import common.FileWriter;
import common.UpdateEvent;
import contract.PieceListContract;
import contract.UpdateEventListener;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class PieceListText implements PieceListContract {
    private int len;            // total text length
    private Piece firstPiece;   // first piece of text
    private File scratch;       // scratch file for adding new stuff

    public int getLen() {
        return len;
    }

    public PieceListText(String filename) {
        scratch = new File("./scratch.txt");

        File firstFile = new File(filename);
        firstPiece = new Piece((int) firstFile.length(), firstFile, 0);
        try {
            loadFrom(new FileInputStream(firstFile));
        } catch (IOException ex) {
            System.err.printf("File %s could not be opened or does not exist!", filename);
        }
    }

    @Override
    public String loadFrom(InputStream in) {
        StringBuilder resultStringBuilder = new StringBuilder();

        try {
            len = in.available();
            try (BufferedReader br
                         = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line).append("\n");
                }
            }

            in.close();

            // TODO: remove -> only for test purposes
            System.out.println(resultStringBuilder);
        } catch (IOException e) {
            len = 0;
        }

        return resultStringBuilder.toString();
    }

    @Override
    public void storeTo(OutputStream out) {
        // TODO: write piecelisttext to file including styles and fonts
        Piece currentPiece = firstPiece;
        StringBuilder result = new StringBuilder();

        while(currentPiece != null) {
            // write current piece content to file
            try {
                result.append(loadFrom(new FileInputStream(currentPiece.file)));
            } catch (FileNotFoundException ex) {
                System.err.printf("Could not find file %s", currentPiece.file.getName());
            }
            currentPiece = currentPiece.next;
        }

        try (BufferedWriter bw
                     = new BufferedWriter(new OutputStreamWriter(out))) {
            bw.write(result.toString());
        } catch (IOException e) {
            System.err.println("Could not write to file");
        }


    }

    @Override
    public char charAt(int pos) {
        // TODO: return char at position pos
        final Piece piece = getPieceAt(pos);

        String fileContent = getFileContent(piece.file);

        if (pos >= fileContent.length()) {
            return '\0';
        }

        return fileContent.charAt(piece.filePos + pos);
    }

    @Override
    public void insert(int pos, String s) {
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
        FileWriter.write(scratch.getAbsolutePath(), s);

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
        Piece p = firstPiece;

        int len = p.len;
        while (pos > len) {
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

    private Piece getPieceAt(int pos) {
        Piece p = firstPiece;

        int len = p.len;
        while (pos > len && p.next != null) {
            p = p.next;
            len += p.len;
        }

        return p;
    }

    private String getFileContent(File file) {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException ex) {
            System.err.printf("Could not open or read from file %s!", file.getName());
        }

        return resultStringBuilder.toString();
    }

    // update behaviour

    ArrayList<UpdateEventListener> listeners = new ArrayList<>();

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
