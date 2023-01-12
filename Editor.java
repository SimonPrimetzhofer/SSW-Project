import piecelist.PieceListText;
import viewer.PieceListViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.IntStream;

import static javax.swing.JOptionPane.showMessageDialog;

public class Editor {

    public static void main(String[] arg) {
        if (arg.length < 1) {
            System.out.println("-- file name missing");
            return;
        }
        String path = arg[0];
        try {
            FileInputStream s = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            System.out.println("-- file " + path + " not found");
            return;
        }

        JScrollBar scrollBar = new JScrollBar(Adjustable.VERTICAL, 0, 0, 0, 0);
        PieceListText pieceListText = new PieceListText(path);
        PieceListViewer viewer = new PieceListViewer(pieceListText, scrollBar);

        // add find input
        final JTextField textField = new JTextField();
        textField.addActionListener(e -> {
            final String text = e.getActionCommand();
            int index; // index from start where text occurs or -1 if not found

            index = pieceListText.indexOf(text);
            if (index == -1) {
                // show error message to user
                showMessageDialog(viewer, "Text " + text + " not found!");
            } else {
                // update index to found first occurence
                viewer.setCaret(index);
                viewer.getScrollBar().setValue(index);
            }

            // reset input
            textField.setText("");
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", textField);
        panel.add("Center", viewer);
        panel.add("East", scrollBar);

        JFrame frame = new JFrame(path);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // file chooser for save and open
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        JMenuBar menuBar = new JMenuBar();
        // File actions
        JMenu fileActionMenu = new JMenu("File");
        JMenuItem openFile = new JMenuItem("Open");
        openFile.addActionListener(e -> {
            if (fileChooser.showOpenDialog(viewer) == JFileChooser.APPROVE_OPTION) {
                Editor.main(new String[]{fileChooser.getSelectedFile().getAbsolutePath()});
            }

        });
        JMenuItem saveFile = new JMenuItem("Save");
        saveFile.addActionListener(e -> pieceListText.save());

        fileActionMenu.add(openFile);
        fileActionMenu.add(saveFile);

        // Edit actions
        JMenu editActionMenu = new JMenu("Edit");
        JMenuItem cut = new JMenuItem("Cut");
        cut.addActionListener(e -> {
            if (viewer.getSelection() != null) {
                viewer.cut(viewer.getSelection().beg.tpos, viewer.getSelection().end.tpos);
            }
        });

        JMenuItem copy = new JMenuItem("Copy");
        copy.addActionListener(e -> {
            if (viewer.getSelection() != null) {
                viewer.copy(viewer.getSelection().beg.tpos, viewer.getSelection().end.tpos);
            }
        });

        JMenuItem paste = new JMenuItem(("Paste"));
        paste.addActionListener(e -> {
            if (viewer.getCaret() != null) {
                viewer.paste();
            }
        });

        editActionMenu.add(cut);
        editActionMenu.add(copy);
        editActionMenu.add(paste);

        // Font menu
        JMenu fontMenu = new JMenu("Fonts");
        Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
                .filter(font -> font.getFontName().equals(font.getFamily()))
                .limit(15)
                .forEach(font -> {
                    JMenuItem fontItem = new JMenuItem(font.getFontName());
                    fontItem.addActionListener(e -> viewer.updateFont(e.getActionCommand()));
                    fontMenu.add(fontItem);
                });

        // size menu
        JMenu sizeMenu = new JMenu("Size");
        IntStream.iterate(6, num -> num + 4).limit(15).forEach(num -> {
            JMenuItem sizeItem = new JMenuItem(String.valueOf(num));
            sizeItem.addActionListener(e -> viewer.updateSize(Integer.parseInt(e.getActionCommand())));
            sizeMenu.add(sizeItem);
        });

        JMenu styleMenu = new JMenu("Style");
        JMenuItem normal = new JMenuItem("Plain");
        normal.addActionListener(e -> viewer.updateStyle(Font.PLAIN));
        JMenuItem bold = new JMenuItem("Bold");
        bold.addActionListener(e -> viewer.updateStyle(Font.BOLD));
        JMenuItem italic = new JMenuItem("Italic");
        italic.addActionListener(e -> viewer.updateStyle(Font.ITALIC));
        styleMenu.add(normal);
        styleMenu.add(bold);
        styleMenu.add(italic);

        // add menus
        menuBar.add(fileActionMenu);
        menuBar.add(editActionMenu);
        menuBar.add(fontMenu);
        menuBar.add(sizeMenu);
        menuBar.add(styleMenu);

        frame.setJMenuBar(menuBar);

        frame.setSize(700, 800);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                viewer.resizeEditor();
            }
        });
        frame.setResizable(true);
        scrollBar.grabFocus();
        frame.setContentPane(panel);
        frame.setVisible(true);
        frame.getContentPane().repaint();
    }

}
