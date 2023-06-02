import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class HTMLEditor extends JFrame implements ActionListener {

    private JTextPane textPane;
    private JTextArea lineNumbersTextArea;
    private JFileChooser fileChooser;
    private JTree domTree;
    private List<String> reservedWords = Arrays.asList(
            "html", "head", "body", "title", "div", "p", "span", "a", "img", "table",
            "tr", "td", "ul", "ol", "li", "form", "input", "button"
            // Agrega aquí más palabras reservadas de HTML
    );

    public HTMLEditor() {
        setTitle("HTML Editor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear el área de texto
        textPane = new JTextPane();
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textPane.getDocument().addDocumentListener(new SyntaxHighlighter());
        textPane.addKeyListener(new KeyClosingTagListener());

        // Crear el área de texto para los números de línea
        lineNumbersTextArea = new JTextArea("1");
        lineNumbersTextArea.setBackground(Color.LIGHT_GRAY);
        lineNumbersTextArea.setEditable(false);
        lineNumbersTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Crear el JScrollPane y establecer su tamaño preferido
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setRowHeaderView(lineNumbersTextArea);

        // Crear el árbol del DOM
        domTree = new JTree();
        domTree.setFont(new Font("Monospaced", Font.PLAIN, 12));
        domTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Crear el JScrollPane para el árbol del DOM
        JScrollPane treeScrollPane = new JScrollPane(domTree);
        treeScrollPane.setPreferredSize(new Dimension(200, 0));

        // Crear el menú y los elementos
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Archivo");
        JMenuItem newMenuItem = new JMenuItem("Nuevo");
        JMenuItem openMenuItem = new JMenuItem("Abrir");
        JMenuItem saveMenuItem = new JMenuItem("Guardar");
        JMenuItem saveAsMenuItem = new JMenuItem("Guardar Como");
        JMenuItem searchMenuItem = new JMenuItem("Buscar");
        JMenuItem printMenuItem = new JMenuItem("Imprimir");
        JMenuItem exitMenuItem = new JMenuItem("Salir");

        // Agregar los elementos al menú
        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(searchMenuItem);
        fileMenu.add(printMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        // Asociar los eventos de los elementos del menú
        newMenuItem.addActionListener(this);
        openMenuItem.addActionListener(this);
        saveMenuItem.addActionListener(this);
        saveAsMenuItem.addActionListener(this);
        searchMenuItem.addActionListener(this);
        printMenuItem.addActionListener(this);
        exitMenuItem.addActionListener(this);

        // Obtener el contenedor principal
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(treeScrollPane, BorderLayout.EAST);
        setJMenuBar(menuBar);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HTMLEditor::new);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Nuevo")) {
            nuevoDocumento();
        } else if (command.equals("Abrir")) {
            abrirDocumento();
        } else if (command.equals("Guardar")) {
            guardarDocumento();
        } else if (command.equals("Guardar Como")) {
            guardarDocumentoComo();
        } else if (command.equals("Buscar")) {
            buscarTexto();
        } else if (command.equals("Imprimir")) {
            imprimirDocumento();
        } else if (command.equals("Salir")) {
            salir();
        }
    }

    private void nuevoDocumento() {
        textPane.setText("");
    }

    private void abrirDocumento() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                textPane.setText(sb.toString());
                updateDOMTree();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void guardarDocumento() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(textPane.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void guardarDocumentoComo() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(textPane.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void imprimirDocumento() {
        try {
            // Imprimir el contenido del JTextPane
            textPane.print();
        } catch (PrinterException e) {
            e.printStackTrace();
        }
    }

    private void salir() {
        System.exit(0);
    }

    private void updateDOMTree() {
        String html = textPane.getText();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("html");
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        domTree.setModel(treeModel);

        // Lógica básica de análisis del HTML
        // Aquí se puede mejorar para casos más complejos
        int startIndex = html.indexOf('<');
        while (startIndex >= 0) {
            int endIndex = html.indexOf('>', startIndex);
            if (endIndex >= 0) {
                String tag = html.substring(startIndex + 1, endIndex);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(tag);
                treeModel.insertNodeInto(node, root, root.getChildCount());
                startIndex = html.indexOf('<', endIndex);
            } else {
                break;
            }
        }
    }

    private void buscarTexto() {
        String searchTerm = JOptionPane.showInputDialog(this, "Ingrese el texto a buscar:");
        if (searchTerm != null && !searchTerm.isEmpty()) {
            buscarEnTexto(searchTerm);
        }
    }

    private void buscarEnTexto(String searchTerm) {
        Document document = textPane.getDocument();
        int startIndex = 0;
        try {
            while (startIndex + searchTerm.length() <= document.getLength()) {
                String text = document.getText(startIndex, searchTerm.length());
                if (text.equalsIgnoreCase(searchTerm)) {
                    textPane.setCaretPosition(startIndex);
                    textPane.moveCaretPosition(startIndex + searchTerm.length());
                    textPane.requestFocusInWindow();
                    return;
                }
                startIndex++;
            }
            JOptionPane.showMessageDialog(this, "Texto no encontrado.");
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private class SyntaxHighlighter implements DocumentListener {

        private StyleContext styleContext;
        private AttributeSet keywordStyle;
        private AttributeSet normalStyle;

        public SyntaxHighlighter() {
            styleContext = StyleContext.getDefaultStyleContext();
            keywordStyle = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLUE);
            normalStyle = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        }

        private void applyHighlighting(Document document) throws BadLocationException {
            String text = document.getText(0, document.getLength());

            // Remover estilos anteriores
            StyledDocument styledDocument = textPane.getStyledDocument();
            styledDocument.setCharacterAttributes(0, text.length(), normalStyle, true);

            // Resaltar palabras reservadas
            for (String word : reservedWords) {
                int index = 0;
                while (index >= 0) {
                    index = text.indexOf(word, index);
                    if (index >= 0) {
                        int endIndex = index + word.length();
                        boolean isWordBoundary = isWordBoundary(text, index, endIndex);
                        if (isWordBoundary) {
                            styledDocument.setCharacterAttributes(index, word.length(), keywordStyle, true);
                        }
                        index = endIndex;
                    }
                }
            }

            // Actualizar los números de línea
            updateLineNumbers();
        }

        private void updateLineNumbers() {
            Document doc = textPane.getDocument();
            int lineCount = doc.getDefaultRootElement().getElementCount();
            StringBuilder sb = new StringBuilder();

            for (int i = 1; i <= lineCount; i++) {
                sb.append(i).append("\n");
            }

            lineNumbersTextArea.setText(sb.toString());
        }

        private boolean isWordBoundary(String text, int startIndex, int endIndex) {
            if (startIndex == 0 && endIndex == text.length()) {
                return true;
            } else if (startIndex == 0) {
                char nextChar = text.charAt(endIndex);
                return !Character.isLetterOrDigit(nextChar);
            } else if (endIndex == text.length()) {
                char prevChar = text.charAt(startIndex - 1);
                return !Character.isLetterOrDigit(prevChar);
            } else {
                char prevChar = text.charAt(startIndex - 1);
                char nextChar = text.charAt(endIndex);
                return !Character.isLetterOrDigit(prevChar) && !Character.isLetterOrDigit(nextChar);
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(() -> {
                try {
                    applyHighlighting(e.getDocument());
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            });
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(() -> {
                try {
                    applyHighlighting(e.getDocument());
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            });
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // No se usa para documentos de texto sin formato
        }
    }

    
  private class KeyClosingTagListener extends KeyAdapter {
    private static final String[] SELF_CLOSING_TAGS = {
            "br", "hr", "img", "input", "link", "meta"
    };

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == '>') {
            insertClosingTag();
        }
    }

    private void insertClosingTag() {
        int caretPosition = textPane.getCaretPosition();
        Document doc = textPane.getDocument();
        StringBuilder sb = new StringBuilder();

        try {
            // Obtener el contenido del documento antes del caret
            sb.append(doc.getText(0, caretPosition));

            // Obtener el último tag abierto
            String lastOpenedTag = getLastOpenedTag(sb.toString());

            // Verificar si el último tag es un tag de autocierre
            if (isSelfClosingTag(lastOpenedTag)) {
                // No se necesita cerrar el tag
                return;
            }

            // Obtener el nombre del último tag abierto
            String tagName = getTagName(lastOpenedTag);

            // Construir el tag de cierre correspondiente
            String closingTag = "</" + tagName + ">";

            // Insertar el carácter > seguido del tag de cierre en el documento
            sb.append(">").append(closingTag);

            // Obtener el contenido del documento después del caret
            sb.append(doc.getText(caretPosition, doc.getLength() - caretPosition));

            // Reemplazar el contenido del documento con el nuevo contenido
            doc.remove(0, doc.getLength());
            doc.insertString(0, sb.toString(), null);

            // Actualizar el caretPosition
            textPane.setCaretPosition(caretPosition + closingTag.length() + 2);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private String getLastOpenedTag(String text) {
        int lastIndex = text.lastIndexOf('<');
        int nextSpaceIndex = text.indexOf(' ', lastIndex);

        if (nextSpaceIndex != -1) {
            return text.substring(lastIndex + 1, nextSpaceIndex);
        } else {
            return text.substring(lastIndex + 1);
        }
    }

    private String getTagName(String tag) {
        int index = tag.indexOf('>');
        if (index != -1) {
            return tag.substring(0, index);
        } else {
            return tag;
        }
    }

    private boolean isSelfClosingTag(String tag) {
        for (String selfClosingTag : SELF_CLOSING_TAGS) {
            if (selfClosingTag.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }
}

}