package cs3410.project.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static FileSystem fs;

    public static void main(String[] args) {
        File toCopy = null;
        String toCopyDestination = "";
        String toExtract = "";
        String toExtractDestination = "";
        boolean forceExtract = false;
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
                fs.mftContainer.createNewFile();
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
            } else {
                FSFile target = fs.newFile(parent, targetName);
                try {
                    target.write(Files.readAllBytes(toCopy.toPath()));
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!toExtract.isEmpty()) {
            FileSystemObject file = fs.getObject(toExtract);
            if(file == null) {
                throw new RuntimeException("File does not exist: " + toExtract);
            }
            if(file instanceof FSDirectory) {
                // TODO
                throw new RuntimeException("Unimplemented");
            }
            File out = new File(toExtractDestination);
            if(out.exists() && !forceExtract) {
                System.err.println(
                        "File already exists: " + out.getAbsolutePath() + "\nRun again with the -f flag to overwrite");
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
    }
}
