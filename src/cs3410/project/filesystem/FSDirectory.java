package cs3410.project.filesystem;

public class FSDirectory extends FileSystemObject {
    public FileSet children = new FileSet();

    public FSDirectory(FSDirectory parent, String name) {
        super(parent, name);
    }

    public FSDirectory(String name) {
        super(name);
    }

    /**
     * @return The child of the directory with the given name if it exists,
     *         otherwise <tt>null</tt>.
     */
    public FileSystemObject getChild(String name) {
        for(FileSystemObject child : children) {
            if(child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }

    /**
     * @return The index of the given child within the {@link #children} FileSet.
     * This method exists for convenience when using indexed-based methods in
     * the <tt>BrowserFrame</tt>'s <tt>JTable</tt>s.
     * <br>
     * Returns -1 if the given object is not a child of the FSDirectory.
     */
    public int getChildIndex(FileSystemObject child) {
        for(int i = 0; i < children.size(); i++) {
            if(children.get(i).equals(child)) return i;
        }
        return -1;
    }

    /**
     * @return The sum of the sizes of the FSDirectory's children.
     */
    @Override
    public int getSize() {
        int sum = 0;
        for(FileSystemObject child : children) {
            sum += child.getSize();
        }
        return sum;
    }
}
