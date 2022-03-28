package cs3410.project.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cs3410.project.filesystem.gui.BrowserFrame;

public class FileSystem {
    public final FSDirectory root = new FSDirectory(null, "");
    /**
     * This file stores the contents of the files contained within the file system
     */
    public File container;
    /**
     * This file stores the structure of the file system
     */
    public File mftContainer;
    private byte[] lastIOHash;

    public FileSystem(File container) {
        this.container = container;
        this.mftContainer = new File(container.getParentFile(), container.getName() + ".mft");
    }

    /**
     * Performs an action on all of the descendants of <tt>root</tt>.
     */
    public void traverse(FSDirectory root, FSAction action) {
        action.run(root);
        for(FileSystemObject obj : root.children) {
            if(obj.isDirectory()) {
                traverse((FSDirectory) obj, action);
            } else {
                action.run(obj);
            }
        }
    }

    /**
     * @param parent The parent directory of the new file
     * @param name The name of the new file
     * @return The <tt>FSFile</tt> if it was created successfully, otherwise <tt>null</tt>
     */
    public FSFile newFile(FSDirectory parent, String name) {
        if(name.isEmpty()) return null;
        if(exists(parent, name)) return null;
        FSFile file = new FSFile(name);
        parent.children.add(file);
        file.parent = parent;
        return file;
    }

    /**
     * @param parent The parent directory of the new directory
     * @param name The name of the new directory
     * @return The <tt>FSDirectory</tt> if it was created successfully, otherwise <tt>null</tt>
     */
    public FSDirectory newDirectory(FSDirectory parent, String name) {
        if(name.isEmpty()) return null;
        if(exists(parent, name)) return null;
        FSDirectory dir = new FSDirectory(name);
        parent.children.add(dir);
        dir.parent = parent;
        return dir;
    }

    /**
     * Recursively creates the ancestors of an object at a given path.
     * 
     * @param root 
     * @param path The path, relative to <tt>root</tt>, to the file whose ancestors
     *             should be created
     * @return The immediate parent of the object at <tt>path</tt>
     * @throws RuntimeException If one of the parents already exists and is not a directory.
     */
    public FSDirectory createParents(FSDirectory root, String path) {
        if(path.charAt(0) == '/') {
            root = this.root;
            path = path.substring(1);
        }
        if(path.isEmpty()) return null;
        if(!path.contains("/")) return root;
        String[] splitPath = path.split("/", 2);
        if(splitPath.length == 0) return null;
        FileSystemObject child = null;
        if(exists(root, splitPath[0])) {
            child = root.getChild(splitPath[0]);
        } else {
            child = newDirectory(root, splitPath[0]);
        }
        if(!child.isDirectory()) {
            throw new RuntimeException(
                    "Error while recursively creating parents: '" + child.name + "' exists and is not a directory");
        }
        return createParents((FSDirectory) child, splitPath[1]);
    }

    /**
     * @return Whether an object exists at the given path
     */
    public boolean exists(String path) {
        return getObject(path) != null;
    }

    /**
     * @return If <tt>parent</tt> has a child with the given name
     */
    public boolean exists(FSDirectory parent, String name) {
        return exists(parent.getPath() + "/" + name);
    }

    /**
     * Writes the file system to the container file on the disk.
     * <br><br>
     * Each file consists of four bytes containing the size of the file's contents,
     * in bytes, followed by the contents. For example, a file consisting of only
     * the text "test 123" would be written as follows:
     * <pre>
     * 00 00 00 08 74 65 73 74 20 31 32 33
     * ^---------^ ^---------------------^
     *    size             content
     * </pre>
     * The structure of the file system is written to a separate file. The location
     * of each object in the file system is represented by the object's absolute path
     * preceded by a single byte to determine if the object is a <tt>FSFile</tt> or
     * <tt>FSDirectory</tt> (<tt>0x46</tt> for files and <tt>0x44</tt> for directories)
     * and, if the object is a <tt>FSFile</tt>, the starting index of the object within
     * the container file. Following the index is the length of the object's path, in
     * bytes.
     * <br><br>
     * For example, a <tt>FSFile</tt> with the name "file.txt" and starting index 100
     * that is an immediate child of file system's root would be written as follows:
     * <pre>
     * 46 00 00 00 64 00 00 00 09 2F 74 65 73 74 2E 74 78 74
     * ^  ^---------^ ^---------^ ^------------------------^
     * F     index     path size             path
     * </pre>
     * @see #readContainer()
     * @throws IOException
     */
    public void writeContainer() throws IOException {
        byte[] data = getFullBytes();
        if(!hasChangedSinceLastIO(data)) return;
        Files.write(container.toPath(), data);
        try {
            lastIOHash = MessageDigest.getInstance("SHA-1").digest(data);
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private byte[] getFullBytes() {
        Map<FSFile, Integer> dataStartIndices = new HashMap<FSFile, Integer>();
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        traverse(root, new FSAction() {
            int index = 0;

            @Override
            public void run(FileSystemObject obj) {
                if(!obj.isDirectory()) {
                    try {
                        FSFile file = (FSFile) obj;
                        dataStartIndices.put(file, index);
                        dataStream.write(Utils.intToBytes(file.getTotalSize()));
                        index += 4;
                        if(file.data != null) {
                            dataStream.write(file.data);
                            index += file.getSize();
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        byte[] data = dataStream.toByteArray();

        ByteArrayOutputStream mftStream = new ByteArrayOutputStream();
        traverse(root, new FSAction() {
            @Override
            public void run(FileSystemObject obj) {
                if(obj.isRoot()) return;
                try {
                    mftStream.write(obj.isDirectory() ? (byte) 0x44 : (byte) 0x46);
                    if(!obj.isDirectory()) {
                        mftStream.write(Utils.intToBytes(dataStartIndices.get(obj)));
                    }
                    mftStream.write(Utils.intToBytes(obj.getPath().length()));
                    mftStream.write(obj.getPath().getBytes());
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });
        byte[] mft = mftStream.toByteArray();

        byte[] output = new byte[data.length + mft.length + 8];
        System.arraycopy(Utils.intToBytes(mft.length), 0, output, 0, 4);
        System.arraycopy(Utils.intToBytes(data.length), 0, output, 4, 4);
        System.arraycopy(mft, 0, output, 8, mft.length);
        System.arraycopy(data, 0, output, 8 + mft.length, data.length);
        return output;
    }

    /**
     * Reads the container file from the disk.
     * @see #writeContainer()
     * @throws IOException
     */
    public void readContainer() throws IOException {
        byte[] full = Files.readAllBytes(container.toPath());
        try {
            lastIOHash = MessageDigest.getInstance("SHA-1").digest(full);
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] mft = new byte[Utils.bytesToInt(Arrays.copyOfRange(full, 0, 4))];
        byte[] data = new byte[Utils.bytesToInt(Arrays.copyOfRange(full, 4, 8))];
        System.arraycopy(full, 8, mft, 0, mft.length);
        System.arraycopy(full, 8 + mft.length, data, 0, data.length);
        for(int i = 0; i < mft.length; i++) {
            boolean isDirectory = mft[i] == (byte) 0x44;
            i++;
            int startIndex = 0;
            if(!isDirectory) {
                startIndex = Utils.bytesToInt(Arrays.copyOfRange(mft, i, i + 4));
                i += 4;
            }
            int pathSize = Utils.bytesToInt(Arrays.copyOfRange(mft, i, i + 4));
            i += 4;
            String path = new String(Arrays.copyOfRange(mft, i, i + pathSize));
            FSDirectory parent = getParent(path);
            String name = path.substring(path.lastIndexOf('/') + 1, path.length());
            if(isDirectory) {
                newDirectory(parent, name);
            } else {
                FSFile file = newFile(parent, name);
                int size = Utils.bytesToInt(Arrays.copyOfRange(data, startIndex, startIndex + 4));
                if(size > 0) {
                    file.write(Arrays.copyOfRange(data, startIndex + 4, startIndex + size));
                } else {
                    file.write(new byte[0]);
                }
            }
            i += pathSize - 1;
        }
    }

    /**
     * @param path The path of the object to get the parent of
     * @return The parent <tt>FSDirectory</tt> of the object at the specified path,
     *         or <tt>null</tt> if the path is empty or relative
     */
    public FSDirectory getParent(String path) {
        if(path.isEmpty() || !path.contains("/")) return null;
        if(path.indexOf('/') == path.lastIndexOf('/')) return root;
        return (FSDirectory) getObject(path.substring(0, path.lastIndexOf('/')));
    }

    /**
     * @param parent The root of the subtree to search for the object
     * @param path The path to an object
     * @return The <tt>FileSystemObject</tt> at the specified path if it exists, otherwise <tt>null</tt>
     * @see #getObject(String)
     */
    public FileSystemObject getObject(FSDirectory parent, String path) {
        if(path.charAt(0) == '/') {
            parent = root;
            path = path.substring(1);
        }
        if(path.isEmpty()) return null;
        if(!path.contains("/")) return parent.getChild(path);
        String[] splitPath = path.split("/", 2);
        if(splitPath.length == 0) return null;
        FileSystemObject obj = parent.getChild(splitPath[0]);
        if(obj == null) {
            return null;
        } else if(obj.isDirectory()) {
            return getObject((FSDirectory) obj, splitPath[1]);
        } else {
            return null;
        }
    }

    /**
     * @param path The path to an object
     * @return The <tt>FileSystemObject</tt> at the specified path if it exists, otherwise <tt>null</tt>.
     * @see #getObject(FSDirectory, String)
     */
    public FileSystemObject getObject(String path) {
        return getObject(root, path);
    }

    private void getTreeAsString(FSDirectory root, int depth, int index, StringBuilder sb, boolean utf8) {
        if(depth != 0) {
            for(int i = 0; i < depth - 1; i++) {
                sb.append(utf8 ? "\u2502  " : "|   ");
            }
            if(index == root.parent.children.size() - 1) {
                sb.append(utf8 ? "\u2514\u2500\u2500" : "+-- ");
            } else {
                sb.append(utf8 ? "\u251C\u2500\u2500" : "+-- ");
            }
        }
        sb.append(root.name);
        sb.append("/");
        sb.append("\n");
        index = 0;
        for(FileSystemObject obj : root.children) {
            if(obj.isDirectory()) {
                getTreeAsString((FSDirectory) obj, depth + 1, index, sb, utf8);
            } else {
                for(int i = 0; i < depth; i++) {
                    sb.append(utf8 ? "\u2502  " : "|   ");
                }
                if(index == root.children.size() - 1) {
                    sb.append(utf8 ? "\u2514\u2500\u2500" : "+-- ");
                } else {
                    sb.append(utf8 ? "\u251C\u2500\u2500" : "+-- ");
                }
                sb.append(obj.name);
                sb.append("\n");
            }
            index++;
        }
    }

    /**
     * @param root The <tt>FSDirectory</tt> from which to start the traversal
     * @param utf8 If true, the tree is created using box drawing characters.
     *             Otherwise, ASCII characters (<tt>|+-</tt>) are used.
     */
    public String getTreeAsString(FSDirectory root, boolean utf8) {
        StringBuilder sb = new StringBuilder();
        getTreeAsString(root, 0, 0, sb, utf8);
        return sb.toString();
    }

    public static void load(File container) throws IOException {
        if(container.isDirectory()) throw new RuntimeException("Container may not be a directory");
        Main.fs = new FileSystem(container);
        if(Main.fs.container.exists()) {
            Main.fs.readContainer();
        } else {
            Main.fs.container.createNewFile();
            Main.fs.mftContainer.createNewFile();
        }
    }

    public boolean hasChangedSinceLastIO(byte[] currentData) {
        if(lastIOHash == null) return true;
        byte[] currentHash;
        try {
            currentHash = MessageDigest.getInstance("SHA-1").digest(currentData);
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            return true;
        }
        for(int i = 0; i < currentHash.length; i++) {
            if(currentHash[i] != lastIOHash[i]) return true;
        }
        return false;
    }

    public boolean importFile(File toImport, String destination) throws IOException {
        if(destination.contains("%s")) {
            destination = String.format(destination, toImport.getName());
        }
        if(destination.isEmpty()) {
            destination = "/" + toImport.getName();
        }
        String[] splitPath = destination.split("/");
        String targetName = splitPath[splitPath.length - 1];
        FSDirectory parent = createParents(root, destination);
        if(parent.getChild(splitPath[splitPath.length - 1]) != null) {
            return false;
        } else {
            FSFile target = newFile(parent, targetName);
            target.write(Files.readAllBytes(toImport.toPath()));
            return true;
        }
    }

    public boolean importFile(File toImport, String destination, BrowserFrame browser) throws IOException {
        boolean r = importFile(toImport, destination);
        browser.update(browser.currentRoot);
        return r;
    }
}
