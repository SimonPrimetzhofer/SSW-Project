import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
		Viewer viewer = new Viewer(new Text(path), scrollBar);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add("Center", viewer);
		panel.add("East", scrollBar);

		JFrame frame = new JFrame(path);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		JMenuBar menuBar = new JMenuBar();

		// File actions
		JMenu fileActionMenu = new JMenu("File");
		JMenuItem openFile = new JMenuItem("Open");
		openFile.addActionListener(e -> {
			// TODO: open file
		});
		JMenuItem saveFile = new JMenuItem("Save");
		saveFile.addActionListener(e -> {
			// TODO save file
		});

		fileActionMenu.add(openFile);
		fileActionMenu.add(saveFile);

		// Edit actions
		JMenu editActionMenu = new JMenu("Edit");
		JMenuItem cut = new JMenuItem("Cut");
		cut.addActionListener(e -> {
			// TODO: cut text
		});

		JMenuItem copy = new JMenuItem("Copy");
		copy.addActionListener(e -> {
			// TODO: copy text
		});

		JMenuItem paste = new JMenuItem(("Paste"));
		paste.addActionListener(e -> {
			// TODO: paste text
		});
		editActionMenu.add(cut);
		editActionMenu.add(copy);
		editActionMenu.add(paste);

		// add menus
		menuBar.add(fileActionMenu);
		menuBar.add(editActionMenu);

		// add find input
		final JTextField textField = new JTextField();
		textField.addActionListener(e -> {
			// TODO: handle input changed
			System.out.println(e.getActionCommand());
		});
		menuBar.add(textField);

		frame.setJMenuBar(menuBar);

		frame.setSize(700, 800);
		frame.setResizable(true);
		frame.setContentPane(panel);
		frame.setVisible(true);
		frame.getContentPane().repaint();
	}

}
