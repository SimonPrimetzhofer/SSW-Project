package piecelist;

import common.FileWriter;
import common.UpdateEvent;
import contract.PieceListContract;
import contract.UpdateEventListener;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class PieceListText implements PieceListContract {
    private final Piece firstPiece;   // first piece of text
    private final File scratch;       // scratch file for adding new stuff
    ArrayList<UpdateEventListener> listeners = new ArrayList<>();
    private int len;            // total text length

    public PieceListText(String filename) {
        scratch = new File("./scratch.txt");
        File firstFile = new File(filename);
        firstPiece = new Piece((int) firstFile.length(), firstFile, 0);
        len = (int) firstFile.length();
    }

    public int getLen() {
        return len;
    }

    @Override
    public void loadFrom(InputStream in) {
        // TODO: Load fonts and styles from text file
        try {
            //len = in.available();
            try (BufferedReader br
                         = new BufferedReader(new InputStreamReader(in))) {
                // get textoffset from file
                int textOffset = br.read();
                // read font and style descriptors
                for (int i = 0; i < textOffset; i += 3){
                    int pieceLen = br.read();
                    int font = br.read();
                    int style = br.read();
                }

                //

                String line;
                while ((line = br.readLine()) != null) {
                }
            }

            in.close();
        } catch (IOException e) {
            len = 0;
        }
    }

    @Override
    public void storeTo(OutputStream out) {
        Piece currentPiece = firstPiece;
        StringBuilder result = new StringBuilder();

        while (currentPiece != null) {
            // write current piece content to file
            try {
                // TODO: don't append whole file (only starting from filePos up to filePos + len
                // TODO: don't append \0 in middle of file
                result.append(readFileContent(new FileInputStream(currentPiece.file)));
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

    private String readFileContent(final FileInputStream in) {
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
        } catch (IOException e) {
            len = 0;
        }

        return resultStringBuilder.toString();
    }

    @Override
    public char charAt(int pos) {
        if (pos < 0 || pos >= len) return '\0';

        Piece p = firstPiece;

        int len = p.len;
        while (pos > len) {
            p = p.next;
            len += p.len;
        }

        String fileContent = getFileContent(p.file);

        return fileContent.charAt(p.filePos + pos);
    }

    @Override
    public void insert(int pos, char ch) {
        // split at pos for inserting text here
        Piece p = split(pos);

        // TODO: check if not last piece on scratch file
        if (!(p.file == scratch && p.next == null)) {
            Piece q = new Piece(0, scratch, (int) scratch.length());

            // new piece is predecessor of previous piece
            q.next = p.next;
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

        notify(new UpdateEvent(pos, pos, String.valueOf(ch)));
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
            int index = getFileContent(p.file).indexOf(pattern);
            if (index > -1) {
                return p.filePos + index;
            }

            p = p.next;
        }

        return -1;
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
