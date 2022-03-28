package cs3410.project.filesystem.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import cs3410.project.filesystem.FSFile;
import cs3410.project.filesystem.FileSystemObject;
import cs3410.project.filesystem.Main;

public class BrowserContextMenu extends JPopupMenu {
    public BrowserContextMenu(BrowserFrame browser) {
        if(browser.table.getSelectedRowCount() == 0) {
            return;
        }
        if(browser.table.getSelectedRowCount() == 1) {
            FileSystemObject selected = browser.getObjectAtRowIndex(browser.table.getSelectedRow());
            JMenuItem open = new JMenuItem("Open");
            open.addActionListener(e -> {
                for(int i : browser.table.getSelectedRows()) {
                    browser.open(browser.getObjectAtRowIndex(i));
                }
            });
            add(open);
            JMenuItem rename = new JMenuItem("Rename");
            rename.addActionListener(e -> {
                String newName = JOptionPane.showInputDialog("Enter a name for the file:", selected.name);
                if(newName == null || newName.isBlank()) return;
                newName = newName.trim();
                if(newName.equals(selected.name)) return;
                if(browser.currentRoot.getChild(newName) != null) {
                    JOptionPane.showMessageDialog(this,
                            String.format("An object with the name \"%s\" already exists", newName),
                            "Failed to rename file", JOptionPane.ERROR_MESSAGE);
                } else {
                    selected.rename(newName);
                    browser.update(browser.currentRoot);
                }
            });
            add(rename);
            if(selected instanceof FSFile) {
                JMenuItem export = new JMenuItem("Export");
                export.addActionListener(e -> {
                    BrowserFrame.FILE_CHOOSER.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    BrowserFrame.FILE_CHOOSER.setFileFilter(browser.defaultFileFilter);
                    int rt = BrowserFrame.FILE_CHOOSER.showOpenDialog(this);
                    if(rt == JFileChooser.APPROVE_OPTION) {
                        File destination = new File(BrowserFrame.FILE_CHOOSER.getSelectedFile(), selected.name);
                        if(destination.exists()) {
                            JOptionPane.showMessageDialog(this, "The destination file already exists",
                                    "Failed to export file", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        try {
                            Main.fs.exportFile((FSFile) selected, destination, false);
                        } catch(IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "An exception occurred while exporting the file",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                add(export);
            }
        }
        JMenuItem delete = new JMenuItem("Delete");
        delete.addActionListener(e -> {
            browser.deleteSelected();
        });
        add(delete);
    }
}
