package cs3410.project.filesystem.browser;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
                    int row = table.rowAtPoint(e.getPoint());
                    if(row == 0 && !currentRoot.isRoot()) {
                        update(currentRoot.parent);
                        return;
                    }
                    FileSystemObject obj = currentRoot.children.get(row - (currentRoot.isRoot() ? 0 : 1));
                    if(obj instanceof FSDirectory) {
                        update((FSDirectory) obj);
                    } else {
                        // TODO open file
                    }
                }
            }
        });
        table.setFont(new Font(table.getFont().getName(), Font.PLAIN, 14));
        scrollPane.setPreferredSize(new Dimension(800, 800));
        scrollPane.setViewportView(table);
        add(scrollPane);
        pack();
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
                        return obj.name;
                    case 1: {
                        if(obj instanceof FSFile) {
                            if(!obj.name.contains(".")) return "File";
                            String extension = obj.name.substring(obj.name.lastIndexOf('.', obj.name.length()));
                            try {
                                Process proc0 = Runtime.getRuntime().exec(
                                        "reg query HKEY_CURRENT_USER\\SOFTWARE\\Classes\\" + extension + " /v \"\"");
                                BufferedReader stdin0 = new BufferedReader(
                                        new InputStreamReader(proc0.getInputStream()));
                                String s = null, association = "";
                                while((s = stdin0.readLine()) != null) {
                                    if(s.contains("(Default)")) {
                                        association = s;
                                    }
                                }
                                stdin0.close();
                                if(association.isEmpty()) return "File";
                                association = association.substring(association.indexOf("REG_SZ") + 6).trim();
                                Process proc1 = Runtime.getRuntime().exec(
                                        "reg query HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\" + association + " /v \"\"");
                                BufferedReader stdin1 = new BufferedReader(
                                        new InputStreamReader(proc1.getInputStream()));
                                s = null;
                                String description = "";
                                while((s = stdin1.readLine()) != null) {
                                    if(s.contains("(Default)")) {
                                        description = s;
                                    }
                                }
                                stdin1.close();
                                if(description.isEmpty()) return "File";
                                return description.substring(description.indexOf("REG_SZ") + 6).trim();
                            } catch(IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            return "Directory";
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
}
