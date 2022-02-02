package cs3410.project.filesystem;

public abstract class FileSystemObject implements Comparable<FileSystemObject> {
    public FileSystemObject parent;
    public String name;

    public FileSystemObject(String name) {
        this.name = name;
    }

    public String getPath() {
        StringBuilder sb = new StringBuilder();
        if(!this.isRoot()) sb.append('/');
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
}