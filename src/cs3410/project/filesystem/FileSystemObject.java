package cs3410.project.filesystem;

public abstract class FileSystemObject implements Comparable<FileSystemObject> {
    public FSDirectory parent;
    public String name;

    public FileSystemObject(FSDirectory parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public FileSystemObject(String name) {
        this(Main.fs.root, name);
    }

    public String getPath() {
        if(this.isRoot()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append('/');
        sb.append(name);
        if(parent != null) sb.insert(0, parent.getPath());
        return sb.toString();
    }

    public boolean isOrphan() {
        return parent == null || Main.fs.exists(this.getPath());
    }

    public boolean isRoot() {
        return this.equals(Main.fs.root);
    }

    @Override
    public int compareTo(FileSystemObject obj) {
        return this.name.compareTo(obj.name);
    }

    public String toString() {
        return this.isRoot() ? "/" : getPath();
    }
}
