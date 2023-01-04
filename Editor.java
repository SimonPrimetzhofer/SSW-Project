import piecelist.PieceListText;
import viewer.PieceListViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
			// TODO: schaun ma moi
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
			int index = pieceListText.indexOf(text);

			if (index == -1) {
				// show error message to user
				showMessageDialog(null, "Text " + text + " not found!");
				// reset caret to start
				viewer.setCaret(0);
			} else {
				// update index to found first occurence
				viewer.setCaret(index);
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

		MenuBar menuBar = new MenuBar();
		// File actions
		Menu fileActionMenu = new Menu("File");
		MenuItem openFile = new MenuItem("Open");
		openFile.addActionListener(e -> {
			final int action = fileChooser.showOpenDialog(null);


			if (action == JFileChooser.APPROVE_OPTION) {
				Editor.main(new String[]{fileChooser.getSelectedFile().getAbsolutePath()});

				/*try {
					pieceListText.loadFrom(new FileInputStream(fileChooser.getSelectedFile().getAbsolutePath()));
				} catch (FileNotFoundException fileNotFoundException) {
					System.err.println("Trouble opening file...");
				}*/
			}

		});
		MenuItem saveFile = new MenuItem("Save");
		saveFile.addActionListener(e -> {
			final int action = fileChooser.showSaveDialog(null);

			if (action == JFileChooser.APPROVE_OPTION) {
				try {
					pieceListText.storeTo(new FileOutputStream(fileChooser.getSelectedFile().getAbsolutePath(), false));
				} catch (FileNotFoundException fileNotFoundException) {
					System.err.println("Trouble opening file...");
				}
			}
		});

		fileActionMenu.add(openFile);
		fileActionMenu.add(saveFile);

		// Edit actions
		Menu editActionMenu = new Menu("Edit");
		MenuItem cut = new MenuItem("Cut");
		cut.addActionListener(e -> {
			// TODO: cut text
		});

		MenuItem copy = new MenuItem("Copy");
		copy.addActionListener(e -> {
			// TODO: copy text
		});

		MenuItem paste = new MenuItem(("Paste"));
		paste.addActionListener(e -> {
			// TODO: paste text
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
				fontItem.addActionListener(e -> {
					// TODO: set font active for current selection
				});
				fontMenu.add(fontItem);
			});

		// size menu
		Menu sizeMenu = new Menu("Size");
		IntStream.iterate(6, num -> num + 1).limit(15).forEach(num -> {
			MenuItem sizeItem = new MenuItem(String.valueOf(num));
			sizeItem.addActionListener(e -> {
				// TODO: set size for current selection
			});

			sizeMenu.add(sizeItem);
		});

		Menu styleMenu = new Menu("Style");
		MenuItem normal = new MenuItem("Plain");
		normal.addActionListener(e -> {
			// TODO: set style to Font.PLAIN
		});
		MenuItem bold = new MenuItem("Bold");
		bold.addActionListener(e -> {
			// TODO: set style to Font.BOLD
		});
		MenuItem italic = new MenuItem("Italic");
		italic.addActionListener(e -> {
			// TODO: set style to Font.ITALIC
		});
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
		frame.setResizable(true);
		frame.setContentPane(panel);
		frame.setVisible(true);
		frame.getContentPane().repaint();
	}

}
