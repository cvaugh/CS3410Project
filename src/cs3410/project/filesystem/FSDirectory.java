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

    public int getChildIndex(FileSystemObject child) {
        for(int i = 0; i < children.size(); i++) {
            if(children.get(i).equals(child)) return i;
        }
        return -1;
    }

    @Override
    public int getSize() {
        int sum = 0;
        for(FileSystemObject child : children) {
            sum += child.getSize();
        }
        return sum;
    }
}
