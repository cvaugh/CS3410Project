package cs3410.project.filesystem;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class FSFile extends FileSystemObject {
    public int startPosition;
    public byte[] data;
    public boolean writing = false;
    public Map<String, String> meta = new TreeMap<>();

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

    /**
     * Marks the file for deletion.
     * <br>
     * After this method is called, the file will not be saved when
     * {@link FileSystem#writeContainer()} is called and its contents are nullified.
     */
    public void delete() {
        startPosition = -1;
        Arrays.fill(data, (byte) 0);
    }

    /**
     * @return The size of the file's content.
     */
    @Override
    public int getSize() {
        return data == null ? 0 : data.length;
    }

    /**
     * @return The actual size of this file on the disk.
     */
    public int getTotalSize() {
        return getSize() + 4;
    }
}
