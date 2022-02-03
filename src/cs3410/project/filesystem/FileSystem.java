package cs3410.project.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.SortedMap;
import java.util.TreeMap;

public class FileSystem {
    /** Signifies the start of a file */
    public static final byte[] FILE_START_MARKER = new byte[] { (byte) 0xFF, (byte) 0xFF };
    /** Signifies the end of the file size (in bytes) following FILE_START_MARKER */
    public static final byte[] FILE_SIZE_MARKER = new byte[] { (byte) 0xAA, (byte) 0xAA };
    /** Signifies the end of a file */
    public static final byte[] FILE_END_MARKER = new byte[] { (byte) 0xDE, (byte) 0xAD, (byte) 0xFE, (byte) 0xED };
    public final FSDirectory root = new FSDirectory(null, "");
    public File container;

    public FileSystem(File container) {
        this.container = container;
    }

    public void traverse(FSDirectory root, FSAction action) {
        action.run(root);
        for(FileSystemObject obj : root.children) {
            if(obj instanceof FSDirectory) {
                traverse((FSDirectory) obj, action);
            } else {
                action.run(obj);
            }
        }
    }

    public FSFile newFile(FSDirectory parent, String name) {
        if(name.isEmpty()) return null;
        if(exists(parent, name)) return null;
        FSFile file = new FSFile(name);
        parent.children.add(file);
        file.parent = parent;
        return file;
    }

    public FSDirectory newDirectory(FSDirectory parent, String name) {
        if(name.isEmpty()) return null;
        if(exists(parent, name)) return null;
        FSDirectory dir = new FSDirectory(name);
        parent.children.add(dir);
        dir.parent = parent;
        return dir;
    }

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
        if(!(child instanceof FSDirectory)) {
            throw new RuntimeException(
                    "Error while recursively creating parents: '" + child.name + "' exists and is not a directory");
        }
        return createParents((FSDirectory) child, splitPath[1]);
    }

    public boolean exists(String path) {
        return getObject(path) != null;
    }

    public boolean exists(FSDirectory parent, String name) {
        return exists(parent.getPath() + "/" + name);
    }

    public void writeContainer() throws IOException {
        SortedMap<Integer, FSFile> files = new TreeMap<>();
        traverse(root, new FSAction() {
            @Override
            public void run(FileSystemObject obj) {
                if(obj instanceof FSFile) {
                    files.put(((FSFile) obj).startPosition, (FSFile) obj);
                    // TODO: write directories
                }
            }
        });
        int totalSize = 0;
        for(int key : files.keySet()) {
            totalSize += files.get(key).getTotalSize();
        }
        byte[] output = new byte[totalSize];
        for(int key : files.keySet()) {
            int index = files.get(key).startPosition;
            System.arraycopy(FILE_START_MARKER, 0, output, index, FILE_START_MARKER.length);
            index += FILE_START_MARKER.length;
            System.arraycopy(ByteBuffer.allocate(4).putInt(files.get(key).data.length).array(), 0, output, index, 4);
            index += 4;
            System.arraycopy(FILE_SIZE_MARKER, 0, output, index, FILE_SIZE_MARKER.length);
            index += FILE_SIZE_MARKER.length;
            System.arraycopy(files.get(key).data, 0, output, index, files.get(key).data.length);
            index += files.get(key).data.length;
            System.arraycopy(FILE_END_MARKER, 0, output, index, FILE_END_MARKER.length);
            index += FILE_END_MARKER.length;
        }
        Files.write(container.toPath(), output);
    }

    public void readContainer() throws IOException {
        byte[] input = Files.readAllBytes(container.toPath());
        // TODO
    }

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
        } else if(obj instanceof FSDirectory) {
            return getObject((FSDirectory) obj, splitPath[1]);
        } else {
            return null;
        }
    }

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
            if(obj instanceof FSDirectory) {
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

    public void getTreeAsString(FSDirectory root, boolean utf8) {
        StringBuilder sb = new StringBuilder();
        getTreeAsString(root, 0, 0, sb, utf8);
        System.out.println(sb);
    }

    public int findIndexFor(FSFile file) {
        int totalSize = file.getTotalSize();
        SortedMap<Integer, FSFile> files = new TreeMap<>();
        traverse(root, new FSAction() {
            @Override
            public void run(FileSystemObject obj) {
                if(obj instanceof FSFile) {
                    FSFile f = (FSFile) obj;
                    if(f.startPosition == -1) return;
                    files.put(f.startPosition, f);
                }
            }
        });
        int prevEnd = 0;
        for(int key : files.keySet()) {
            if(files.get(key).writing) {
                prevEnd = key;
                continue;
            }
            if(key - prevEnd >= totalSize) {
                return prevEnd;
            }
        }
        totalSize = 0;
        for(int key : files.keySet()) {
            if(!files.get(key).writing) totalSize += files.get(key).getTotalSize();
        }
        return totalSize;
    }
}
