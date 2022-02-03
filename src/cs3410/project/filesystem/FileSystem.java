package cs3410.project.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSystem {
    public final FSDirectory root = new FSDirectory("");
    private File container;

    public FileSystem(File container) {
        this.container = container;
    }

    public List<FileSystemObject> traverse(FSDirectory parent) {
        List<FileSystemObject> list = new ArrayList<FileSystemObject>();
        list.add(parent);
        for(FileSystemObject obj : parent.children) {
            if(obj instanceof FSDirectory) {
                list.addAll(traverse((FSDirectory) obj));
            } else {
                list.add(obj);
            }
        }
        return list;
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
}