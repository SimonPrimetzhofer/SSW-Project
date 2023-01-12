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

            index = 4; //pieceListText.indexOf(text, viewer.getCaret().tpos);
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

        MenuBar menuBar = new MenuBar();
        // File actions
        Menu fileActionMenu = new Menu("File");
        MenuItem openFile = new MenuItem("Open");
        openFile.addActionListener(e -> {
            if (fileChooser.showOpenDialog(viewer) == JFileChooser.APPROVE_OPTION) {
                Editor.main(new String[]{fileChooser.getSelectedFile().getAbsolutePath()});
            }

        });
        MenuItem saveFile = new MenuItem("Save");
        saveFile.addActionListener(e -> pieceListText.save());

        fileActionMenu.add(openFile);
        fileActionMenu.add(saveFile);

        // Edit actions
        Menu editActionMenu = new Menu("Edit");
        MenuItem cut = new MenuItem("Cut");
        cut.addActionListener(e -> {
            if (viewer.getSelection() != null) {
                viewer.cut(viewer.getSelection().beg.tpos, viewer.getSelection().end.tpos);
            }
        });

        MenuItem copy = new MenuItem("Copy");
        copy.addActionListener(e -> {
            if (viewer.getSelection() != null) {
                viewer.copy(viewer.getSelection().beg.tpos, viewer.getSelection().end.tpos);
            }
        });

        MenuItem paste = new MenuItem(("Paste"));
        paste.addActionListener(e -> {
            if (viewer.getCaret() != null) {
                viewer.paste();
            }
        });

        editActionMenu.add(cut);
        editActionMenu.add(copy);
        editActionMenu.add(paste);

        // Font menu
        Menu fontMenu = new Menu("Fonts");
        Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
                .filter(font -> font.getFontName().equals(font.getFamily()))
                .limit(15)
                .forEach(font -> {
                    MenuItem fontItem = new MenuItem(font.getFontName());
                    fontItem.addActionListener(e -> viewer.updateFont(e.getActionCommand()));
                    fontMenu.add(fontItem);
                });

        // size menu
        Menu sizeMenu = new Menu("Size");
        IntStream.iterate(6, num -> num + 4).limit(15).forEach(num -> {
            MenuItem sizeItem = new MenuItem(String.valueOf(num));
            sizeItem.addActionListener(e -> viewer.updateSize(Integer.parseInt(e.getActionCommand())));
            sizeMenu.add(sizeItem);
        });

        Menu styleMenu = new Menu("Style");
        MenuItem normal = new MenuItem("Plain");
        normal.addActionListener(e -> viewer.updateStyle(Font.PLAIN));
        MenuItem bold = new MenuItem("Bold");
        bold.addActionListener(e -> viewer.updateStyle(Font.BOLD));
        MenuItem italic = new MenuItem("Italic");
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

        frame.setMenuBar(menuBar);

        frame.setSize(700, 800);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                viewer.resizeEditor();
            }
        });
        frame.setResizable(true);
        frame.setContentPane(panel);
        frame.setVisible(true);
        frame.getContentPane().repaint();
    }

}
