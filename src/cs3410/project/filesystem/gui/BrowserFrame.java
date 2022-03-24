package cs3410.project.filesystem.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import cs3410.project.filesystem.FSDirectory;
import cs3410.project.filesystem.FSFile;
import cs3410.project.filesystem.FileSystemObject;
import cs3410.project.filesystem.Main;
import cs3410.project.filesystem.Utils;

public class BrowserFrame extends JFrame {
    private static final long serialVersionUID = -6275492324105494374L;

    private static final Map<String, String> MIME_CACHE = new HashMap<>();
    private static final Map<String, String> DESCRIPTION_CACHE = new HashMap<>();
    private static final Map<String, Integer> ICON_CACHE = new HashMap<>();
    private static boolean isMetadataLoaded = false;
    private final JTable table = new JTable();
    private final JScrollPane scrollPane = new JScrollPane(table);
    private final JPanel sidebar = new JPanel();
    private final JButton deleteSelected = new JButton("Delete Selected");
    public FSDirectory currentRoot;

    public BrowserFrame() {
        try {
            loadMetadata();
        } catch(IOException e) {
            e.printStackTrace();
        }
        setTitle(Main.fs.container.getName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    Main.fs.writeContainer();
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
                BrowserFrame.this.setVisible(false);
                BrowserFrame.this.dispose();
                if(!Main.isControlFrameOpen) System.exit(0);
            }
        });
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 800));
        sidebar.setFont(table.getFont());
        JButton newFile = new JButton("Create File");
        newFile.setPreferredSize(new Dimension(180, 20));
        newFile.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Enter a name for the file:");
            if(name == null || name.isBlank()) return;
            name = name.trim();
            if(currentRoot.getChild(name) != null) {
                JOptionPane.showMessageDialog(this,
                        String.format("An object with the name \"%s\" already exists", name), "Failed to create file",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                currentRoot.children.add(new FSFile(currentRoot, name));
                update(currentRoot);
            }
        });
        sidebar.add(newFile);
        JButton newDirectory = new JButton("Create Directory");
        newDirectory.setPreferredSize(newFile.getPreferredSize());
        newDirectory.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Enter a name for the directory:");
            if(name == null || name.isBlank()) return;
            name = name.trim();
            if(currentRoot.getChild(name) != null) {
                JOptionPane.showMessageDialog(this,
                        String.format("An object with the name \"%s\" already exists", name),
                        "Failed to create directory", JOptionPane.ERROR_MESSAGE);
            } else {
                currentRoot.children.add(new FSDirectory(currentRoot, name.trim()));
                update(currentRoot);
            }
        });
        sidebar.add(newDirectory);
        deleteSelected.setPreferredSize(newFile.getPreferredSize());
        deleteSelected.setEnabled(false);
        deleteSelected.addActionListener(e -> {
            List<FileSystemObject> toRemove = new ArrayList<>();
            for(int row : table.getSelectedRows()) {
                if(row == 0 && currentRoot.isRoot()) continue;
                toRemove.add(currentRoot.children.get(row - (currentRoot.isRoot() ? 0 : 1)));
            }
            currentRoot.children.removeAll(toRemove);
            update(currentRoot);
        });
        sidebar.add(deleteSelected);
        add(sidebar);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {
                    // TODO context menu
                } else if(e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if(row == 0 && !currentRoot.isRoot()) {
                        update(currentRoot.parent);
                        return;
                    }
                    FileSystemObject obj = currentRoot.children.get(row - (currentRoot.isRoot() ? 0 : 1));
                    if(obj.isDirectory()) {
                        update((FSDirectory) obj);
                    } else {
                        // TODO open file
                    }
                }
                deleteSelected.setEnabled(table.getSelectedRowCount() > 0
                        && (table.getRowCount() == 1 || (table.getRowCount() > 1 && table.getSelectedRow() != 0)));
            }
        });
        table.setFont(new Font(table.getFont().getName(), Font.PLAIN, 14));
        scrollPane.setPreferredSize(new Dimension(800, 800));
        scrollPane.setViewportView(table);
        add(scrollPane);
        pack();
        setLocationRelativeTo(null);
        update(Main.fs.root);
        table.setRowHeight(table.getRowHeight() + 4);
    }

    private void update(FSDirectory root) {
        currentRoot = root;
        setTitle(Main.fs.container.getName() + " > " + currentRoot);
        table.setModel(new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return root.children.size() + (root.isRoot() ? 0 : 1);
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                if(rowIndex == 0 && !root.isRoot()) {
                    switch(columnIndex) {
                    case 0:
                        return "\u2B8D Parent Directory";
                    case 1:
                        return "Directory";
                    case 2:
                        return "";
                    default:
                        return "";
                    }
                } else {
                    FileSystemObject obj = root.children.get(rowIndex - (root.isRoot() ? 0 : 1));
                    switch(columnIndex) {
                    case 0:
                        return String.format("%c %s", obj.isDirectory() ? 0x1F4C1 : getIcon((FSFile) obj), obj.name);
                    case 1: {
                        if(obj.isDirectory()) {
                            return "Directory";
                        } else {
                            return getDescription((FSFile) obj);
                        }
                    }
                    case 2:
                        return Utils.humanReadableSize(obj.getSize());
                    default:
                        return "";
                    }
                }
            }
        });
        table.getColumnModel().getColumn(0).setHeaderValue("Name");
        table.getColumnModel().getColumn(1).setHeaderValue("Type");
        table.getColumnModel().getColumn(2).setHeaderValue("Size");
        table.getColumnModel().getColumn(0).setPreferredWidth(450);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
    }

    private static String getMimeType(FSFile file) {
        if(!file.name.contains(".")) return "";
        return MIME_CACHE.getOrDefault(file.name.substring(file.name.lastIndexOf('.', file.name.length())), "");
    }

    private static int getIcon(FSFile file) {
        if(!file.name.contains(".")) return 0x1F4C4;
        return ICON_CACHE.getOrDefault(getMimeType(file), 0x1F4C4);
    }

    private static String getDescription(FSFile file) {
        if(!file.name.contains(".")) return "File";
        return DESCRIPTION_CACHE.getOrDefault(getMimeType(file), "File");
    }

    private static void loadMetadata() throws IOException {
        if(isMetadataLoaded) return;
        File assetsDir = new File("assets");
        if(!assetsDir.exists()) return;
        File mimeFile = new File(assetsDir, "mime-types.txt");
        File descriptionFile = new File(assetsDir, "type-names.txt");
        File iconFile = new File(assetsDir, "type-icons.txt");
        for(String line : Files.readAllLines(mimeFile.toPath())) {
            String[] split = line.split(":", 2);
            String[] extensions = split[1].split(" ");
            for(String extension : extensions) {
                MIME_CACHE.put(extension, split[0]);
            }
        }
        for(String line : Files.readAllLines(descriptionFile.toPath())) {
            String[] split = line.split(":", 2);
            DESCRIPTION_CACHE.put(split[0], split[1]);
        }
        for(String line : Files.readAllLines(iconFile.toPath())) {
            String[] split = line.split(":", 2);
            ICON_CACHE.put(split[0], Integer.parseInt(split[1], 16));
        }
        isMetadataLoaded = true;
    }
}
