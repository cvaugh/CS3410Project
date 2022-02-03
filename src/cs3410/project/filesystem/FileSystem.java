package cs3410.project.filesystem;

import java.io.File;

public class FileSystem {
    public final FSDirectory root = new FSDirectory(null, "");
    private File container;

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
        if(!exists(parent, name)) return null;
        FSFile file = new FSFile(name);
        parent.children.add(file);
        file.parent = parent;
        return file;
    }

    public FSDirectory newDirectory(FSDirectory parent, String name) {
        if(name.isEmpty()) return null;
        if(!exists(parent, name)) return null;
        FSDirectory dir = new FSDirectory(name);
        parent.children.add(dir);
        dir.parent = parent;
        return dir;
    }

    public boolean exists(String path) {
        return getObject(path) == null;
    }

    public boolean exists(FSDirectory parent, String name) {
        return exists(parent.getPath() + "/" + name);
    }

    public void writeContainer() {
        // TODO
    }

    public void readContainer() {
        // TODO
    }

    public FileSystemObject getObject(FSDirectory parent, String path) {
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
}