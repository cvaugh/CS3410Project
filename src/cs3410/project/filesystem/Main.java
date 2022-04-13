package cs3410.project.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import cs3410.project.filesystem.gui.BrowserFrame;

public class Main {
    public static FileSystem fs;

    public static void main(String[] args) {
        File container = null;
        File toCopy = null;
        String toCopyDestination = "";
        String toExtract = "";
        String toExtractDestination = "";
        boolean forceExtract = false, printBeforeExit = false;
        if(args.length > 0) {
            // Parse command line arguments
            try {
                for(int i = 0; i < args.length; i++) {
                    // Load the specified file system container
                    if(args[i].equals("-i")) {
                        File file = new File(args[i + 1]);
                        if(file.isDirectory()) {
                            throw new RuntimeException("Container is a directory: " + file.getAbsolutePath());
                        }
                        container = file;
                    }
                    // Copy an external file into the file system
                    if(args[i].equals("-c")) {
                        File file = new File(args[i + 1]);
                        if(!file.exists()) {
                            throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
                        }
                        if(file.isDirectory()) {
                            // TODO
                            throw new RuntimeException("Unimplemented");
                        }
                        toCopy = file;
                    }
                    // Set the internal destination path of the external file
                    if(args[i].equals("-d")) {
                        toCopyDestination = args[i + 1];
                    }
                    // Extract an internal file to the external file system
                    if(args[i].equals("-C")) {
                        toExtract = args[i + 1];
                    }
                    // Set the external destination path of the internal file
                    if(args[i].equals("-D")) {
                        toExtractDestination = args[i + 1];
                    }
                    // Extract file even if the destination file already exists
                    if(args[i].equals("-f")) {
                        forceExtract = true;
                    }
                    // Print the file system before exiting
                    if(args[i].equals("-p")) {
                        printBeforeExit = true;
                    }
                }
            } catch(ArrayIndexOutOfBoundsException e) {
                System.err.println("Invalid arguments: " + String.join(" ", args));
                System.exit(1);
            } catch(RuntimeException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        // If no arguments are supplied, open a GUI.
        if(args.length == 0) {
            BrowserFrame browser = new BrowserFrame();
            browser.setVisible(true);
        } else if(container != null) {
            try {
                FileSystem.load(container);
            } catch(IOException e) {
                e.printStackTrace();
            }

            if(toCopy != null) {
                try {
                    if(!fs.importFile(toCopy, toCopyDestination)) {
                        System.err.println("File already exists at destination: " + toCopyDestination);
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            if(!toExtract.isEmpty()) {
                FileSystemObject file = fs.getObject(toExtract);
                if(file == null) {
                    throw new RuntimeException("File does not exist: " + toExtract);
                }
                if(file.isDirectory()) {
                    // TODO
                    throw new RuntimeException("Unimplemented");
                }
                File out = new File(toExtractDestination);
                if(out.exists() && !forceExtract) {
                    System.err.println("File already exists: " + out.getAbsolutePath()
                            + "\nRun again with the -f flag to overwrite");
                } else if(out.exists() && out.isDirectory()) {
                    System.err.println("Destination is a directory: " + out.getAbsolutePath());
                } else {
                    try {
                        Files.write(out.toPath(), ((FSFile) file).data);
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                fs.writeContainer();
            } catch(IOException e) {
                e.printStackTrace();
            }
            if(printBeforeExit) {
                System.out.println(fs.getTreeAsString(fs.root, true));
            }
        } else {
            System.err.println("Container path must be specified with -i");
            System.exit(1);
        }
    }
}
