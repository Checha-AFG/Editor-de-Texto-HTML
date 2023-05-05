/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

/**
 *
 * @author chech
 */
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class HTMLEditor extends JFrame implements ActionListener {
    private JTextArea textArea;
    private JTextArea lineNumbersTextArea;
    private JFileChooser fileChooser;
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
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.getDocument().addDocumentListener(new LineNumberUpdater());

        // Crear el área de texto para los números de línea
        lineNumbersTextArea = new JTextArea();
        lineNumbersTextArea.setBackground(Color.LIGHT_GRAY);
        lineNumbersTextArea.setEditable(false);
        lineNumbersTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Agregar el área de texto y los números de línea a un JScrollPane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setRowHeaderView(lineNumbersTextArea);

        // Crear el menú y los elementos
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Archivo");
        JMenuItem newMenuItem = new JMenuItem("Nuevo");
        JMenuItem openMenuItem = new JMenuItem("Abrir");
        JMenuItem saveMenuItem = new JMenuItem("Guardar");
        JMenuItem saveAsMenuItem = new JMenuItem("Guardar Como");
        JMenuItem printMenuItem = new JMenuItem("Imprimir");
        JMenuItem exitMenuItem = new JMenuItem("Salir");

        // Agregar los elementos al menú
        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(printMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        // Asociar los eventos de los elementos del menú
        newMenuItem.addActionListener(this);
        openMenuItem.addActionListener(this);
        saveMenuItem.addActionListener(this);
        saveAsMenuItem.addActionListener(this);
        printMenuItem.addActionListener(this);
        exitMenuItem.addActionListener(this);

        // Configurar el contenedor principal
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        setJMenuBar(menuBar);

        // Configurar el selector de archivos
        fileChooser = new JFileChooser();

        setVisible(true);
        
        
    }

    // Método para manejar los eventos del menú
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Nuevo")) {
            textArea.setText("");
        } else if (command.equals("Abrir")) {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                openFile(file);
            }
        } else if (command.equals("Guardar")) {
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                saveFile(file);
            }
        } else if (command.equals("Guardar Como")) {
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                saveFile(file);
            }
        } else if (command.equals("Imprimir")) {
            try {
                textArea.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        } else if (command.equals("Salir")) {
            System.exit(0);
        }
    }

    // Método para abrir un archivo y mostrarlo en el editor de texto
    private void openFile(File file) {
        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            textArea.read(br, null);
            br.close();
            textArea.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para guardar el contenido del editor de texto en un archivo
    private void saveFile(File file) {
        try {
            FileWriter writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            textArea.write(bw);
            bw.close();
            textArea.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clase interna para actualizar el número de líneas al editar el texto
    class LineNumberUpdater implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            updateLineNumbers();
            
        }

        public void removeUpdate(DocumentEvent e) {
            updateLineNumbers();
            
        }

        public void changedUpdate(DocumentEvent e) {
            updateLineNumbers();
            
        }

        private void updateLineNumbers() {
            int totalLines = textArea.getLineCount();
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= totalLines; i++) {
                sb.append(i).append("\n");
            }
            lineNumbersTextArea.setText(sb.toString());
        }
    }
    
    // Método para resaltar las palabras reservadas de HTML
private void highlightReservedWords() {
    DefaultStyledDocument document = (DefaultStyledDocument) textArea.getDocument();
    String content = textArea.getText();

    // Eliminar cualquier estilo anterior
    StyleContext sc = StyleContext.getDefaultStyleContext();
    AttributeSet normalStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);
    document.setCharacterAttributes(0, document.getLength(), normalStyle, true);

    // Resaltar las palabras reservadas
    for (String word : reservedWords) {
        int index = 0;
        while (index >= 0) {
            index = content.indexOf(word, index);
            if (index >= 0) {
                AttributeSet highlightStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLUE);
                document.setCharacterAttributes(index, word.length(), highlightStyle, true);
                index += word.length();
            }
        }
    }
}
    
     public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HTMLEditor();
            }
        });
    }

}



