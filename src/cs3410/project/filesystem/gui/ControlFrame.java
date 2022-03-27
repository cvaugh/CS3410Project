package cs3410.project.filesystem.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import cs3410.project.filesystem.FileSystem;
import cs3410.project.filesystem.Main;

public class ControlFrame extends JFrame {
    private static final long serialVersionUID = -62754963734494374L;

    private static final JFileChooser FILE_CHOOSER = new JFileChooser();
    private final JButton newFS = new JButton("New File System Container");
    private final JButton openFS = new JButton("Open File System Container");
    private final JButton importFile = new JButton("Import File");
    private final JButton exportFile = new JButton("Export File");
    private final JButton openBrowser = new JButton("Open File System Browser");

    // TODO consolidate BrowserFrame and ControlFrame
    public ControlFrame() {
        FILE_CHOOSER.setCurrentDirectory(new File("."));
        setTitle("File System Container Manager");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(Main.fs != null) {
                    try {
                        Main.fs.writeContainer();
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
                ControlFrame.this.setVisible(false);
                ControlFrame.this.dispose();
                Main.isControlFrameOpen = false;
                System.exit(0);
            }
        });
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        setMinimumSize(new Dimension(400, 200));

        newFS.setMinimumSize(new Dimension(400, 40));
        newFS.setMaximumSize(new Dimension(800, 60));
        newFS.setAlignmentX(Component.CENTER_ALIGNMENT);
        newFS.setFont(new Font(newFS.getFont().getName(), Font.BOLD, 24));
        newFS.addActionListener(e -> {
            FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int rt = FILE_CHOOSER.showSaveDialog(this);
            if(rt == JFileChooser.APPROVE_OPTION) {
                if(FILE_CHOOSER.getSelectedFile().exists()) {
                    JOptionPane.showMessageDialog(this, "The selected file already exists", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Main.fs = new FileSystem(FILE_CHOOSER.getSelectedFile());
                setTitle("File System Container Manager: " + Main.fs.container.getName());
                enableButtons();
            }
        });
        add(newFS);
        openFS.setMinimumSize(new Dimension(400, 40));
        openFS.setMaximumSize(new Dimension(800, 60));
        openFS.setAlignmentX(Component.CENTER_ALIGNMENT);
        openFS.setFont(new Font(newFS.getFont().getName(), Font.BOLD, 24));
        openFS.addActionListener(e -> {
            FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int rt = FILE_CHOOSER.showOpenDialog(this);
            if(rt == JFileChooser.APPROVE_OPTION) {
                try {
                    FileSystem.load(FILE_CHOOSER.getSelectedFile());
                    setTitle("File System Container Manager: " + Main.fs.container.getName());
                    enableButtons();
                } catch(IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "An exception occurred while loading the container", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(openFS);
        importFile.setMinimumSize(newFS.getMinimumSize());
        importFile.setMaximumSize(newFS.getMaximumSize());
        importFile.setAlignmentX(newFS.getAlignmentX());
        importFile.setFont(newFS.getFont());
        importFile.setEnabled(false);
        importFile.addActionListener(e -> {
            FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int rt = FILE_CHOOSER.showOpenDialog(this);
            if(rt == JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(this, "This operation has not yet been implemented", "Unimplemented",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        add(importFile);
        exportFile.setMinimumSize(newFS.getMinimumSize());
        exportFile.setMaximumSize(newFS.getMaximumSize());
        exportFile.setAlignmentX(newFS.getAlignmentX());
        exportFile.setFont(newFS.getFont());
        exportFile.setEnabled(false);
        exportFile.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "This operation has not yet been implemented", "Unimplemented",
                    JOptionPane.WARNING_MESSAGE);
        });
        add(exportFile);
        openBrowser.setMinimumSize(newFS.getMinimumSize());
        openBrowser.setMaximumSize(newFS.getMaximumSize());
        openBrowser.setAlignmentX(newFS.getAlignmentX());
        openBrowser.setFont(newFS.getFont());
        openBrowser.setEnabled(false);
        openBrowser.addActionListener(e -> {
            BrowserFrame browserFrame = new BrowserFrame();
            browserFrame.setVisible(true);
        });
        add(openBrowser);
        pack();
        setLocationRelativeTo(null);
    }

    private void enableButtons() {
        importFile.setEnabled(true);
        exportFile.setEnabled(true);
        openBrowser.setEnabled(true);
    }
}
