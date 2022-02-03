package cs3410.project.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static FileSystem fs;

    public static void main(String[] args) {
        File toCopy = null;
        String toCopyDestination = "";
        if(args.length > 0) {
            // Parse command line arguments
            try {
                for(int i = 0; i < args.length; i++) {
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
                }
            } catch(ArrayIndexOutOfBoundsException e) {
                System.err.println("Invalid arguments: " + String.join(" ", args));
                System.exit(1);
            } catch(RuntimeException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        fs = new FileSystem(new File("test.fs"));
        try {
            if(fs.container.exists()) {
                fs.readContainer();
            } else {
                fs.container.createNewFile();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        if(toCopy != null) {
            if(toCopyDestination.isEmpty()) {
                toCopyDestination = "/" + toCopy.getName();
            }
            String[] splitPath = toCopyDestination.split("/");
            String targetName = splitPath[splitPath.length - 1];
            FSDirectory parent = fs.createParents(fs.root, toCopyDestination);
            if(fs.exists(toCopyDestination)) {
                System.err.println("File already exists at destination: " + toCopyDestination);
            }
            FSFile target = fs.newFile(parent, targetName);
            try {
                target.write(Files.readAllBytes(toCopy.toPath()));
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        fs.getTreeAsString(fs.root, true);

        try {
            fs.writeContainer();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: register shell extension handler for reading from/reading to file
    // system container using Windows file explorer
    // public static native void registerShellExtensionHandler();
}
