package cs3410.project.filesystem.gui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;

import cs3410.project.filesystem.FSDirectory;
import cs3410.project.filesystem.FSFile;
import cs3410.project.filesystem.FileSystem;
import cs3410.project.filesystem.FileSystemObject;
import cs3410.project.filesystem.Main;
import cs3410.project.filesystem.Utils;

public class BrowserFrame extends JFrame {
    private static final long serialVersionUID = -6275492324105494374L;

    protected static final JFileChooser FILE_CHOOSER;
    static {
        // Attempts to set the JFileChooser's look and feel to the system style
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }
        FILE_CHOOSER = new JFileChooser();
        // Resets the look and feel to Java's default for all other Swing components
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    private static final FileFilter FS_FILTER = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.getName().endsWith(".fs");
        }

        @Override
        public String getDescription() {
            return "File System Containers (.fs)";
        }
    };
    private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir") + File.separator + "fstemp");
    private static final Map<String, String> MIME_CACHE = new HashMap<>();
    private static final Map<String, String> DESCRIPTION_CACHE = new HashMap<>();
    private static final Map<String, Integer> ICON_CACHE = new HashMap<>();
    private static boolean isMetadataLoaded = false;
    protected final JTable table = new JTable();
    private final JScrollPane scrollPane = new JScrollPane(table);
    private final JPanel sidebar = new JPanel();
    private final JMenu fileMenu = new JMenu("File");
    private final JMenu viewMenu = new JMenu("View");
    private final JMenu toolsMenu = new JMenu("Tools");
    private final JMenu containerMenu = new JMenu("Container");
    private final JMenuItem importFile = new JMenuItem("Import File");
    private final JButton newFile = new JButton("Create File");
    private final JButton newDirectory = new JButton("Create Directory");
    private final JButton deleteSelected = new JButton("Delete Selected");
    private final JButton search = new JButton("Search");
    private final JTextField urlBar = new JTextField();
    public FSDirectory currentRoot;
    protected FileFilter defaultFileFilter;

    public BrowserFrame() {
        if(!TEMP_DIR.exists()) TEMP_DIR.mkdirs();
        TEMP_DIR.deleteOnExit();
        defaultFileFilter = FILE_CHOOSER.getFileFilter();
        FILE_CHOOSER.setCurrentDirectory(new File("."));
        try {
            loadMetadata();
        } catch(IOException e) {
            e.printStackTrace();
        }
        setTitle("File System Browser");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // If a file system has been loaded, save it before exiting.
                if(Main.fs != null) {
                    try {
                        Main.fs.writeContainer();
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
                BrowserFrame.this.setVisible(false);
                BrowserFrame.this.dispose();
                System.exit(0);
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                sidebar.setMaximumSize(new Dimension(getWidth() / 4, Integer.MAX_VALUE));
                scrollPane.setMaximumSize(new Dimension((getWidth() / 4) * 3, Integer.MAX_VALUE));
                // revalidate() is necessary here due to a bug in the Windows implementation of
                // Swing when maximizing a JFrame.
                sidebar.revalidate();
                scrollPane.revalidate();
            }
        });
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        // The following code is very redundant due to how Swing handles adding
        // components.
        JMenuBar menuBar = new JMenuBar();
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.setEnabled(false);
        JMenuItem menuItem = new JMenuItem("New File");
        menuItem.setMnemonic(KeyEvent.VK_F);
        menuItem.addActionListener(e -> {
            newFile();
        });
        fileMenu.add(menuItem);
        menuItem = new JMenuItem("New Directory");
        menuItem.setMnemonic(KeyEvent.VK_D);
        menuItem.addActionListener(e -> {
            newDirectory();
        });
        fileMenu.add(menuItem);
        menuItem = new JMenuItem("Delete Selected");
        menuItem.setMnemonic(KeyEvent.VK_R);
        menuItem.addActionListener(e -> {
            deleteSelected();
        });
        fileMenu.add(menuItem);
        fileMenu.addSeparator();
        menuItem = new JMenuItem("Exit");
        menuItem.setMnemonic(KeyEvent.VK_X);
        menuItem.addActionListener(e -> {
            BrowserFrame.this.dispatchEvent(new WindowEvent(BrowserFrame.this, WindowEvent.WINDOW_CLOSING));
        });
        fileMenu.add(menuItem);
        menuBar.add(fileMenu);
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuItem = new JMenuItem("Increase Font Size");
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.addActionListener(e -> {
            updateFontSize(2);
        });
        viewMenu.add(menuItem);
        menuItem = new JMenuItem("Decrease Font Size");
        menuItem.setMnemonic(KeyEvent.VK_D);
        menuItem.addActionListener(e -> {
            updateFontSize(-2);
        });
        viewMenu.add(menuItem);
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);
        toolsMenu.setEnabled(false);
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        menuItem = new JMenuItem("Search");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(e -> {
            openSearch();
        });
        toolsMenu.add(menuItem);
        menuBar.add(toolsMenu);
        containerMenu.setMnemonic(KeyEvent.VK_C);
        menuItem = new JMenuItem("New Container");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(e -> {
            FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FILE_CHOOSER.setFileFilter(FS_FILTER);
            int rt = FILE_CHOOSER.showSaveDialog(this);
            if(rt == JFileChooser.APPROVE_OPTION) {
                if(FILE_CHOOSER.getSelectedFile().exists()) {
                    JOptionPane.showMessageDialog(this, "The selected file already exists", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Main.fs = new FileSystem(FILE_CHOOSER.getSelectedFile());
                setTitle("File System Container Manager: " + Main.fs.container.getName());
                init();
            }
        });
        containerMenu.add(menuItem);
        menuItem = new JMenuItem("Open Container");
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuItem.addActionListener(e -> {
            FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FILE_CHOOSER.setFileFilter(FS_FILTER);
            int rt = FILE_CHOOSER.showOpenDialog(this);
            if(rt == JFileChooser.APPROVE_OPTION) {
                try {
                    FileSystem.load(FILE_CHOOSER.getSelectedFile());
                    setTitle("File System Container Manager: " + Main.fs.container.getName());
                    init();
                } catch(IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "An exception occurred while loading the container", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        containerMenu.add(menuItem);
        importFile.setEnabled(false);
        importFile.setMnemonic(KeyEvent.VK_I);
        importFile.addActionListener(e -> {
            FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            FILE_CHOOSER.setFileFilter(defaultFileFilter);
            int rt = FILE_CHOOSER.showOpenDialog(this);
            if(rt == JFileChooser.APPROVE_OPTION) {
                try {
                    if(!Main.fs.importFile(FILE_CHOOSER.getSelectedFile(),
                            currentRoot.getPath() + "/" + FILE_CHOOSER.getSelectedFile().getName(), this)) {
                        JOptionPane.showMessageDialog(this, String.format("An internal file with the name \"%s\"",
                                FILE_CHOOSER.getSelectedFile().getName()), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch(IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(this, String.format("An exception occurred while importing \"%s\"",
                            FILE_CHOOSER.getSelectedFile().getName()), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        containerMenu.add(importFile);
        menuBar.add(containerMenu);
        setJMenuBar(menuBar);
        sidebar.setPreferredSize(new Dimension(200, 800));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setFont(table.getFont());
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        newFile.setEnabled(false);
        newFile.addActionListener(e -> {
            newFile();
        });
        newFile.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(newFile);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        newDirectory.setEnabled(false);
        newDirectory.addActionListener(e -> {
            newDirectory();
        });
        newDirectory.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(newDirectory);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        deleteSelected.setEnabled(false);
        deleteSelected.addActionListener(e -> {
            deleteSelected();
        });
        deleteSelected.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(deleteSelected);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        search.setEnabled(false);
        search.addActionListener(e -> {
            openSearch();
        });
        search.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(search);
        urlBar.setPreferredSize(new Dimension(800, 20));
        urlBar.setEnabled(false);
        urlBar.addActionListener(e -> {
            if(urlBar.getText().isBlank()) return;
            FileSystemObject obj = Main.fs.getObject(urlBar.getText().trim());
            if(obj == null) {
                JOptionPane.showMessageDialog(this, String.format("The path \"%s\" does not exist", urlBar.getText()),
                        "Path Not Found", JOptionPane.WARNING_MESSAGE);
            } else {
                open(obj);
            }
        });
        add(urlBar);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(sidebar);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {
                    // Show a context menu when the table is right-clicked
                    new BrowserContextMenu(BrowserFrame.this).show(e.getComponent(), e.getX(), e.getY());
                } else if(e.getClickCount() == 2) {
                    // Open the selected file when the table is double-clicked
                    int row = table.rowAtPoint(e.getPoint());
                    if(row == 0 && !currentRoot.isRoot()) {
                        update(currentRoot.parent);
                        return;
                    }
                    open(getObjectAtRowIndex(row));
                }
                updateButtons();
            }
        });
        table.setFont(new Font(table.getFont().getName(), Font.PLAIN, 14));
        scrollPane.setPreferredSize(new Dimension(800, 800));
        scrollPane.setViewportView(table);
        panel.add(scrollPane);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        table.setRowHeight(table.getRowHeight() + 4);
        updateFontSize(0);
    }

    /**
     * Shows a prompt for creating a new empty file in the current directory.
     */
    private void newFile() {
        String name = JOptionPane.showInputDialog("Enter a name for the file:");
        if(name == null || name.isBlank()) return;
        name = name.trim();
        if(currentRoot.getChild(name) != null) {
            JOptionPane.showMessageDialog(this, String.format("An object with the name \"%s\" already exists", name),
                    "Failed to create file", JOptionPane.ERROR_MESSAGE);
        } else {
            currentRoot.children.add(new FSFile(currentRoot, name));
            update(currentRoot);
        }
    }

    /**
     * Shows a prompt for creating a new directory in the current directory.
     */
    private void newDirectory() {
        String name = JOptionPane.showInputDialog("Enter a name for the directory:");
        if(name == null || name.isBlank()) return;
        name = name.trim();
        if(currentRoot.getChild(name) != null) {
            JOptionPane.showMessageDialog(this, String.format("An object with the name \"%s\" already exists", name),
                    "Failed to create directory", JOptionPane.ERROR_MESSAGE);
        } else {
            currentRoot.children.add(new FSDirectory(currentRoot, name.trim()));
            update(currentRoot);
        }
    }

    /**
     * Deletes the objects currently selected in the JTable from the file system.
     */
    protected void deleteSelected() {
        List<FileSystemObject> toRemove = new ArrayList<>();
        for(int row : table.getSelectedRows()) {
            if(row == 0 && !currentRoot.isRoot()) continue;
            toRemove.add(currentRoot.children.get(row - (currentRoot.isRoot() ? 0 : 1)));
        }
        currentRoot.children.removeAll(toRemove);
        update(currentRoot);
    }

    private void openSearch() {
        SearchFrame searchFrame = new SearchFrame(this);
        searchFrame.setVisible(true);
    }

    /**
     * Opens the given <tt>FileSystemObject</tt>.
     * If the object is a <tt>FSDirectory</tt>, the frame is reloaded with the
     * given object as the new root. If the object is a <tt>FSFile</tt>, it is
     * opened using the application associated with the file type by the
     * operating system.
     * @see #update(FSDirectory)
     */
    public void open(FileSystemObject obj) {
        if(obj.isDirectory()) {
            update((FSDirectory) obj);
        } else {
            try {
                File out = new File(TEMP_DIR, obj.name);
                if(Main.fs.exportFile((FSFile) obj, out, true)) {
                    Desktop.getDesktop().open(out);
                    out.deleteOnExit();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to export file", "Failed to export file",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reloads the frame with the given directory as the new root.
     */
    public void update(FSDirectory root) {
        currentRoot = root;
        setTitle(Main.fs.container.getName() + " > " + currentRoot);
        urlBar.setText(currentRoot.isRoot() ? "/" : currentRoot.getPath());
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
                    FileSystemObject obj = getObjectAtRowIndex(rowIndex);
                    switch(columnIndex) {
                    case 0: // First column: The name of the object prefixed by an icon.
                        return String.format("%c %s", obj.isDirectory() ? 0x1F4C1 : getIcon((FSFile) obj), obj.name);
                    case 1: // Second column: The type of the object.
                        return obj.isDirectory() ? "Directory" : getDescription((FSFile) obj);
                    case 2: // Third column: The size of the object.
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

    protected FileSystemObject getObjectAtRowIndex(int index) {
        return currentRoot.children.get(index - (currentRoot.isRoot() ? 0 : 1));
    }

    private void init() {
        fileMenu.setEnabled(true);
        toolsMenu.setEnabled(true);
        urlBar.setEnabled(true);
        newFile.setEnabled(true);
        newDirectory.setEnabled(true);
        search.setEnabled(true);
        importFile.setEnabled(true);
        setTitle(Main.fs.container.getName());
        update(Main.fs.root);
    }

    protected void updateButtons() {
        deleteSelected.setEnabled(table.getSelectedRowCount() > 0 && (currentRoot.isRoot()
                || (!currentRoot.isRoot() && (table.getRowCount() > 1 && table.getSelectedRow() != 0))));
    }

    /**
     * Increases or decreases the font size of most elements in the frame
     * based on <tt>delta</tt>.
     */
    private void updateFontSize(int delta) {
        if(delta < 0 && table.getFont().getSize() <= 2) return;
        if(delta > 0 && table.getFont().getSize() >= 100) return;
        Font fontPlain = new Font(table.getFont().getName(), Font.PLAIN, table.getFont().getSize() + delta);
        Font fontBold = fontPlain.deriveFont(Font.BOLD);
        for(Component c : sidebar.getComponents()) {
            c.setFont(fontBold);
            if(c instanceof JButton) ((JButton) c).setMargin(new Insets(2, 5, 2, 5));
        }
        table.setFont(fontPlain);
        table.setRowHeight(table.getRowHeight() + delta);
    }

    /**
     * @return The most likely MIME type of the given file based on its extension,
     * or an empty string if no type association exists.
     */
    protected static String getMimeType(FSFile file) {
        if(!file.name.contains(".")) return "";
        return MIME_CACHE.getOrDefault(file.name.substring(file.name.lastIndexOf('.', file.name.length())), "");
    }

    /**
     * @return A <tt>char</tt> to be used as an icon for the given file, based
     * on its extension.
     * @see #getMimeType(FSFile)
     */
    protected static int getIcon(FSFile file) {
        if(!file.name.contains(".")) return 0x1F4C4;
        return ICON_CACHE.getOrDefault(getMimeType(file), 0x1F4C4);
    }

    /**
     * @return A description of the given file's type, based on its extension.
     * @see #getMimeType(FSFile)
     */
    protected static String getDescription(FSFile file) {
        if(!file.name.contains(".")) return "File";
        return DESCRIPTION_CACHE.getOrDefault(getMimeType(file), "File");
    }

    /**
     * Attempts to load the type associations, icons, and descriptions of various file types
     * if an <tt>assets</tt> directory exists in the working directory of the program.
     * @throws IOException
     */
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
