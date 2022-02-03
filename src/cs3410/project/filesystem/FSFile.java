package cs3410.project.filesystem;

public class FSFile extends FileSystemObject {
    private long startPosition;
    private long size;
    private byte[] data;

    public FSFile(FSDirectory parent, String name) {
        super(parent, name);
    }

    public FSFile(String name) {
        super(name);
    }

    public void write(byte[] data) {
        // TODO
    }

    public void append(byte[] data) {
        // TODO
    }

    public void read() {
        // TODO
    }

    public void delete() {
        // TODO
    }
}
