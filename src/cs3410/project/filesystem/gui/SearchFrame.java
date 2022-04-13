package cs3410.project.filesystem.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.PatternSyntaxException;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import cs3410.project.filesystem.FSFile;
import cs3410.project.filesystem.FileSet;
import cs3410.project.filesystem.FileSystemObject;
import cs3410.project.filesystem.Main;
import cs3410.project.filesystem.Utils;

public class SearchFrame extends JFrame {
    private static final long serialVersionUID = -62754923241467574L;

    private final JTable table = new JTable();
    private final JScrollPane scrollPane = new JScrollPane(table);
    private final JPanel sidebar = new JPanel();
    private final JTextField query = new JTextField();
    private final JCheckBox matchCase = new JCheckBox("Match Case");
    private final JCheckBox matchExact = new JCheckBox("Exact Matches Only");
    private final JCheckBox useRegex = new JCheckBox("Regular Expression");
    private FileSystemObject[] results;

    public SearchFrame(BrowserFrame browser) {
        setTitle("Search File System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeFrame();
            }
        });
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 800));
        query.setPreferredSize(new Dimension(180, 20));
        query.addActionListener(e -> {
            search();
        });
        query.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                search();
            }
        });
        sidebar.add(query);
        matchCase.setPreferredSize(new Dimension(180, 20));
        matchCase.addChangeListener(e -> {
            search();
        });
        sidebar.add(matchCase);
        matchExact.setPreferredSize(matchCase.getPreferredSize());
        matchExact.addChangeListener(e -> {
            matchCase.setEnabled(!matchExact.isSelected() && !useRegex.isSelected());
            search();
        });
        sidebar.add(matchExact);
        useRegex.setPreferredSize(matchCase.getPreferredSize());
        useRegex.addChangeListener(e -> {
            matchCase.setEnabled(!useRegex.isSelected() && !matchExact.isSelected());
            matchExact.setEnabled(!useRegex.isSelected());
            search();
        });
        sidebar.add(useRegex);
        add(sidebar);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getClickCount() == 2 && table.getSelectedRowCount() > 0) {
                    browser.currentRoot = results[table.getSelectedRow()].parent;
                    browser.update(browser.currentRoot);
                    int i = browser.currentRoot.getChildIndex(results[table.getSelectedRow()]);
                    browser.table.setRowSelectionInterval(i, i);
                    browser.updateButtons();
                    closeFrame();
                }
            }
        });
        table.setFont(new Font(table.getFont().getName(), Font.PLAIN, 14));
        sidebar.setFont(table.getFont());
        scrollPane.setPreferredSize(new Dimension(800, 800));
        scrollPane.setViewportView(table);
        add(scrollPane);
        pack();
        setLocationRelativeTo(null);
        table.setRowHeight(table.getRowHeight() + 4);
    }

    /**
     * Searches the file system for a file name matching the contents of the
     * {@link #query} field. Results are placed in the {@link #results} array.
     * <br><br>
     * If {@link #useRegex} is checked, the method assumes that the content of
     * the text field is a regular expression.
     * <br>
     * If {@link #matchExact} is checked, the method only matches files whose
     * names match the query exactly.
     * <br>
     * If {@link #matchCase} is checked, the method includes partial maches
     * whose case matches the query's case.
     * <br>
     * If nothing of the above are checked, the method includes partial matches
     * regardless of case.
     */
    private void search() {
        FileSet found = new FileSet();
        if(!query.getText().isBlank()) {
            Main.fs.traverse(Main.fs.root, obj -> {
                if(useRegex.isSelected()) {
                    try {
                        if(obj.name.matches(query.getText())) found.add(obj);
                    } catch(PatternSyntaxException ignore) {}
                } else if(matchExact.isSelected()) {
                    if(obj.name.equals(query.getText())) found.add(obj);
                } else if(matchCase.isSelected()) {
                    if(obj.name.contains(query.getText())) found.add(obj);
                } else {
                    if(obj.name.toLowerCase().contains(query.getText().toLowerCase())) found.add(obj);
                }
            });
        }
        results = new FileSystemObject[found.size()];
        for(int i = 0; i < results.length; i++)
            results[i] = found.get(i);
        update();
    }

    private void update() {
        table.setModel(new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return results.length;
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                FileSystemObject obj = results[rowIndex];
                switch(columnIndex) {
                case 0:
                    return String.format("%c %s", obj.isDirectory() ? 0x1F4C1 : BrowserFrame.getIcon((FSFile) obj),
                            obj.name);
                case 1: {
                    if(obj.isDirectory()) {
                        return "Directory";
                    } else {
                        return BrowserFrame.getDescription((FSFile) obj);
                    }
                }
                case 2:
                    return Utils.humanReadableSize(obj.getSize());
                default:
                    return "";
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

    private void closeFrame() {
        SearchFrame.this.setVisible(false);
        SearchFrame.this.dispose();
    }
}
