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

    /**
     * @return The absolute path of this <tt>FileSystemObject</tt>.
     */
    public String getPath() {
        if(this.isRoot()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append('/');
        sb.append(name);
        if(parent != null) sb.insert(0, parent.getPath());
        return sb.toString();
    }

    public void rename(String newName) {
        if(name.equals(newName)) return;
        this.name = newName;
        parent.children.sort();
    }

    /**
     * @return Whether this object is orphaned from the file system.
     */
    public boolean isOrphan() {
        return parent == null || Main.fs.exists(this.getPath());
    }

    /**
     * @return Whether this object is the file system's root directory.
     */
    public boolean isRoot() {
        return this.equals(Main.fs.root);
    }

    /**
     * @return Whether this object is an instance of <tt>FSDirectory</tt>
     */
    public boolean isDirectory() {
        return this instanceof FSDirectory;
    }

    @Override
    public int compareTo(FileSystemObject obj) {
        return this.name.compareTo(obj.name);
    }

    public String toString() {
        return this.isRoot() ? "/" : getPath();
    }

    /**
     * @return The size of the file's content.
     */
    public abstract int getSize();
}
