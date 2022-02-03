package cs3410.project.filesystem;

import java.io.IOException;
import java.util.Arrays;

public class FSFile extends FileSystemObject {
    public int startPosition;
    public byte[] data;
    public boolean writing = false;

    public FSFile(FSDirectory parent, String name) {
        super(parent, name);
    }

    public FSFile(String name) {
        super(name);
    }

    public void write(byte[] data) throws IOException {
        this.data = data;
        writing = true;
        startPosition = Main.fs.findIndexFor(this);
        writing = false;
        Main.fs.writeContainer();
    }

    public void read() {
        // TODO only read files as needed, rather than keeping everything in memory
    }

    public void delete() {
        startPosition = -1;
        Arrays.fill(data, (byte) 0);
    }

    public int getTotalSize() {
        return (data == null ? 0 : data.length) + 4;
    }
}
