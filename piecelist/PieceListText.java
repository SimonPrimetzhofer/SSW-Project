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
    }

    @Override
    public void loadFrom(InputStream in) {
       /* try {
            loadFrom(new FileInputStream(firstFile));
        } catch (IOException ex) {
            System.err.printf("File %s could not be opened or does not exist!", filename);
        }*/
    }

    @Override
    public void storeTo(OutputStream out) {
        Piece currentPiece = firstPiece;
        StringBuilder result = new StringBuilder();

        while(currentPiece != null) {
            // write current piece content to file
            try {
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
        Piece p = firstPiece;

        int len = p.len;
        while (pos > len && p.next != null) {
            p = p.next;
            len += p.len;
        }

        String fileContent = getFileContent(p.file);

        if (pos >= fileContent.length()) {
            return '\0';
        }

        return fileContent.charAt(p.filePos + pos);
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

        notify(new UpdateEvent(pos, pos, s));
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
        while (pos > len && p.next != null) {
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
