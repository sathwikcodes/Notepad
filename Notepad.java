import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.undo.UndoManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Notepad extends JFrame implements ActionListener {
    private JTextPane textPane = new JTextPane();
    private JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
    private JToolBar toolBar = new JToolBar();
    private JComboBox<String> fontBox;
    private JComboBox<Integer> fontSizeBox;
    private JButton boldButton, italicButton, underlineButton, colorButton, leftButton, centerButton, rightButton, imageButton;
    private JButton saveButton, openButton, undoButton, redoButton, findButton, replaceButton;
    private SimpleAttributeSet attributes = new SimpleAttributeSet();
    private UndoManager undoManager = new UndoManager();

    public Notepad() {
        super("Notepad");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create toolbar
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontBox = new JComboBox<>(fonts);
        fontBox.setSelectedItem("Arial");
        fontBox.addActionListener(this);
        toolBar.add(fontBox);

        Integer[] sizes = new Integer[30];
        for (int i = 0; i < sizes.length; i++) sizes[i] = i + 1;
        fontSizeBox = new JComboBox<>(sizes);
        fontSizeBox.setSelectedItem(12);
        fontSizeBox.addActionListener(this);
        toolBar.add(fontSizeBox);

        boldButton = new JButton(new StyledEditorKit.BoldAction());
        boldButton.setText("B");
        toolBar.add(boldButton);

        italicButton = new JButton(new StyledEditorKit.ItalicAction());
        italicButton.setText("I");
        toolBar.add(italicButton);

        underlineButton = new JButton(new StyledEditorKit.UnderlineAction());
        underlineButton.setText("U");
        toolBar.add(underlineButton);

        colorButton = new JButton("Color");
        colorButton.addActionListener(this);
        toolBar.add(colorButton);

        leftButton = new JButton(new StyledEditorKit.AlignmentAction("Left Align", StyleConstants.ALIGN_LEFT));
        leftButton.setText("L");
        toolBar.add(leftButton);

        centerButton = new JButton(new StyledEditorKit.AlignmentAction("Center Align", StyleConstants.ALIGN_CENTER));
        centerButton.setText("C");
        toolBar.add(centerButton);

        rightButton = new JButton(new StyledEditorKit.AlignmentAction("Right Align", StyleConstants.ALIGN_RIGHT));
        rightButton.setText("R");
        toolBar.add(rightButton);

        imageButton = new JButton("Image");
        imageButton.addActionListener(this);
        toolBar.add(imageButton);

        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        toolBar.add(saveButton);

        openButton = new JButton("Open");
        openButton.addActionListener(this);
        toolBar.add(openButton);

        undoButton = new JButton("Undo");
        undoButton.addActionListener(this);
        toolBar.add(undoButton);

        redoButton = new JButton("Redo");
        redoButton.addActionListener(this);
        toolBar.add(redoButton);

        findButton = new JButton("Find");
        findButton.addActionListener(this);
        toolBar.add(findButton);

        replaceButton = new JButton("Replace");
        replaceButton.addActionListener(this);
        toolBar.add(replaceButton);

        add(toolBar, BorderLayout.NORTH);

        // Create text pane
        textPane.setFont(new Font((String) fontBox.getSelectedItem(), Font.PLAIN, (int) fontSizeBox.getSelectedItem()));
        add(new JScrollPane(textPane), BorderLayout.CENTER);

        // Set up undo manager
        textPane.getDocument().addUndoableEditListener(undoManager);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == fontBox || e.getSource() == fontSizeBox) {
            textPane.setFont(new Font((String) fontBox.getSelectedItem(), Font.PLAIN, (int) fontSizeBox.getSelectedItem()));
        } else if (e.getSource() == colorButton) {
            Color color = JColorChooser.showDialog(this, "Choose a color", textPane.getForeground());
            if (color != null) {
                StyleConstants.setForeground(attributes, color);
                ((StyledDocument) textPane.getDocument()).setCharacterAttributes(textPane.getSelectionStart(), textPane.getSelectionEnd() - textPane.getSelectionStart(), attributes, false);
            }
        } else if (e.getSource() == imageButton) {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getPath();
                ImageIcon image = new ImageIcon(path);
                textPane.insertIcon(image);
            }
        } else if (e.getSource() == saveButton) {
            saveFile();
        } else if (e.getSource() == openButton) {
            openFile();
        } else if (e.getSource() == undoButton) {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } else if (e.getSource() == redoButton) {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        } else if (e.getSource() == findButton) {
            String searchText = JOptionPane.showInputDialog(this, "Find:");
            if (searchText != null) {
                findText(searchText);
            }
        } else if (e.getSource() == replaceButton) {
            String searchText = JOptionPane.showInputDialog(this, "Find:");
            if (searchText != null) {
                String replaceText = JOptionPane.showInputDialog(this, "Replace with:");
                replaceText(searchText, replaceText);
            }
        }
    }

    private void saveFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(textPane.getText());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                textPane.setText(content.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void findText(String searchText) {
        Document document = textPane.getDocument();
        try {
            String text = document.getText(0, document.getLength());
            int index = text.indexOf(searchText);
            if (index != -1) {
                textPane.setCaretPosition(index);
                textPane.moveCaretPosition(index + searchText.length());
            } else {
                JOptionPane.showMessageDialog(this, "Text not found.");
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void replaceText(String searchText, String replaceText) {
        Document document = textPane.getDocument();
        try {
            String text = document.getText(0, document.getLength());
            String newText = text.replace(searchText, replaceText);
            document.remove(0, document.getLength());
            document.insertString(0, newText, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Notepad());
    }
}