package cs3410.project.filesystem;

import java.io.File;

public class Main {
    public static FileSystem fs;

    public static void main(String[] args) {
        fs = new FileSystem(new File("test.fs"));
        fs.newDirectory(fs.root, "usr");
        fs.newDirectory(fs.root, "var");
        FSDirectory etc = fs.newDirectory(fs.root, "etc");
        fs.newFile(etc, "settings.conf");
        FSDirectory misc = fs.newDirectory(etc, "misc");
        fs.newFile(misc, "test.txt");
        for(FileSystemObject obj : fs.traverse(fs.root)) {
            System.out.println(obj);
        }
    }
}