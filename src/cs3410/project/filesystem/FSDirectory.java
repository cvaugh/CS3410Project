package cs3410.project.filesystem;

import java.util.SortedSet;
import java.util.TreeSet;

public class FSDirectory extends FileSystemObject {
    public SortedSet<FileSystemObject> children = new TreeSet<>();

    public FSDirectory(FSDirectory parent, String name) {
        super(parent, name);
    }

    public FSDirectory(String name) {
        super(name);
    }

    public FileSystemObject getChild(String name) {
        for(FileSystemObject child : children) {
            if(child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }
}