package cs3410.project.filesystem.gui;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import cs3410.project.filesystem.FileSystemObject;

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
        }
        JMenuItem delete = new JMenuItem("Delete");
        add(delete);
    }
}
