package cs3410.project.filesystem;

import java.util.Arrays;

public class FSFile extends FileSystemObject {
    public byte[] data;

    public FSFile(FSDirectory parent, String name) {
        super(parent, name);
    }

    public FSFile(String name) {
        super(name);
    }

    /**
     * Sets the file's contents to the given byte array.
     */
    public void write(byte[] data) {
        this.data = data;
    }

    /**
     * Marks the file for deletion.
     * <br>
     * After this method is called, the file will not be saved when
     * {@link FileSystem#writeContainer()} is called and its contents are nullified.
     */
    public void delete() {
        parent.children.remove(this);
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
     * @return The actual size of this file on the disk. 4 is added to the return
     * value of {@link #getSize()} to account for the integer representing the
     * file's size within the file system container's data section.
     * @see FileSystem#getFullBytes()
     */
    public int getTotalSize() {
        return getSize() + 4;
    }
}
