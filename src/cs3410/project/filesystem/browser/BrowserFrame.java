package cs3410.project.filesystem.browser;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
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

    private final JTable table = new JTable();
    private final JScrollPane scrollPane = new JScrollPane(table);
    public FSDirectory currentRoot;

    public BrowserFrame() {
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
                System.exit(0);
            }
        });
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {
                    // TODO context menu
                } else if(e.getClickCount() == 2) {
                    FileSystemObject obj = currentRoot.children.get(table.rowAtPoint(e.getPoint()));
                    if(obj instanceof FSDirectory) {
                        update((FSDirectory) obj);
                    } else {
                        // TODO open file
                    }
                }
            }
        });
        scrollPane.setPreferredSize(new Dimension(800, 800));
        scrollPane.setViewportView(table);
        add(scrollPane);
        pack();
        update(Main.fs.root);
    }

    private void update(FSDirectory root) {
        currentRoot = root;
        setTitle(Main.fs.container.getName() + " > " + currentRoot);
        table.setModel(new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return root.children.size();
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                FileSystemObject obj = root.children.get(rowIndex);
                switch(columnIndex) {
                case 0:
                    return obj.name;
                case 1:
                    return obj instanceof FSFile ? "File" : "Directory";
                case 2:
                    return obj instanceof FSFile ? Utils.humanReadableSize(((FSFile) obj).getSize()) : "";
                default:
                    return "";
                }
            }
        });
        table.getColumnModel().getColumn(0).setHeaderValue("Name");
        table.getColumnModel().getColumn(1).setHeaderValue("Type");
        table.getColumnModel().getColumn(2).setHeaderValue("Size");
        table.getColumnModel().getColumn(0).setPreferredWidth(600);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
    }
}
