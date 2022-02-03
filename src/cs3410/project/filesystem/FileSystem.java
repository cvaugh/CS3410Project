package cs3410.project.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class FileSystem {
    /** Signifies the end of a file */
    public static final byte[] FILE_END_MARKER = new byte[] { (byte) 0xDE, (byte) 0xAD, (byte) 0xFE, (byte) 0xED };
    public final FSDirectory root = new FSDirectory(null, "");
    public File container;
    public File mftContainer;

    public FileSystem(File container) {
        this.container = container;
        this.mftContainer = new File(container.getParentFile(), container.getName() + ".mft");
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
            System.arraycopy(
                    ByteBuffer.allocate(4).putInt(files.get(key).data == null ? 0 : files.get(key).data.length).array(),
                    0, output, index, 4);
            index += 4;
            if(files.get(key).data != null) {
                System.arraycopy(files.get(key).data, 0, output, index, files.get(key).data.length);
                index += files.get(key).data.length;
            }
        }
        Files.write(container.toPath(), output);

        ByteArrayOutputStream mft = new ByteArrayOutputStream();
        traverse(root, new FSAction() {
            @Override
            public void run(FileSystemObject obj) {
                if(obj.isRoot()) return;
                try {
                    mft.write(obj instanceof FSFile ? (byte) 0x46 : (byte) 0x44);
                    if(obj instanceof FSFile) {
                        mft.write(ByteBuffer.allocate(4).putInt(((FSFile) obj).startPosition).array());
                    }
                    mft.write(obj.getPath().getBytes());
                    mft.write(FILE_END_MARKER);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Files.write(mftContainer.toPath(), mft.toByteArray());
    }

    public void readContainer() throws IOException {
        byte[] data = Files.readAllBytes(container.toPath());
        byte[] mft = Files.readAllBytes(mftContainer.toPath());
        List<byte[]> mftSplit = Utils.splitByteArray(mft, FILE_END_MARKER);
        for(byte[] segment : mftSplit) {
            if(segment.length == 0) continue;
            boolean isDirectory = segment[0] == (byte) 0x44;
            String path = new String(Arrays.copyOfRange(segment, isDirectory ? 1 : 5, segment.length));
            FSDirectory parent = getParent(path);
            String name = path.substring(path.lastIndexOf('/') + 1, path.length());
            if(isDirectory) {
                newDirectory(parent, name);
            } else {
                FSFile file = newFile(parent, name);
                int startIndex = Utils.bytesToInt(Arrays.copyOfRange(segment, 1, 5));
                System.out.println(startIndex);
                int size = Utils.bytesToInt(Arrays.copyOfRange(data, startIndex, startIndex + 4));
                System.out.println(size);
                if(size > 0) {
                    file.write(Arrays.copyOfRange(data, startIndex + 4, startIndex + size));
                } else {
                    file.write(new byte[0]);
                }
            }
        }
    }

    public FSDirectory getParent(String path) {
        if(path.isEmpty() || !path.contains("/")) return null;
        if(path.indexOf('/') == path.lastIndexOf('/')) return root;
        return (FSDirectory) getObject(path.substring(0, path.lastIndexOf('/')));
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
