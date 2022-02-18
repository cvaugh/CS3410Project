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
     *         otherwise <tt>null</tt>
     */
    public FileSystemObject getChild(String name) {
        for(FileSystemObject child : children) {
            if(child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }
}
