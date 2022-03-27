package cs3410.project.filesystem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class FileSet implements Collection<FileSystemObject> {
    private FileSystemObject[] objects = new FileSystemObject[0];

    @Override
    public int size() {
        return objects.length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        for(int i = 0; i < size(); i++) {
            if(o.equals(objects[i])) return true;
        }
        return false;
    }

    @Override
    public Iterator<FileSystemObject> iterator() {
        return Arrays.stream(objects).iterator();
    }

    public FileSystemObject get(int index) {
        return objects[index];
    }

    @Override
    public Object[] toArray() {
        return objects;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        return (T[]) objects;
    }

    @Override
    public boolean add(FileSystemObject e) {
        if(e == null || contains(e)) return false;
        fit(1);
        objects[size() - 1] = e;
        sort();
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends FileSystemObject> c) {
        if(c.size() == 0) return false;
        fit(count(c.toArray()));
        int i = 0;
        for(FileSystemObject obj : c) {
            if(contains(obj)) continue;
            objects[size() - 1 - c.size() + i] = obj;
            i++;
        }
        sort();
        return true;
    }

    private int count(Object... objs) {
        if(objs.length == 0) return 0;
        int count = 0;
        for(Object obj : objs) {
            if(!contains(obj)) count++;
        }
        return count;
    }

    private void fit(int count) {
        if(objects.length < size() + count) {
            FileSystemObject[] temp = new FileSystemObject[objects.length + count];
            System.arraycopy(objects, 0, temp, 0, size());
            objects = temp;
        }
    }

    @Override
    public boolean remove(Object o) {
        for(int i = 0; i < size(); i++) {
            if(objects[i].equals(o)) {
                objects[i] = null;
                sort();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c) {
            if(!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if(c.size() == 0) return false;
        boolean changed = false;
        for(Object o : c) {
            for(int i = 0; i < size(); i++) {
                if(o.equals(objects[i])) {
                    objects[i] = null;
                    changed = true;
                }
            }
        }
        if(changed) sort();
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if(c.size() == 0) {
            clear();
            return true;
        }
        FileSystemObject[] temp = new FileSystemObject[size()];
        int i = 0;
        for(Object o : c) {
            for(int j = 0; j < size(); j++) {
                if(objects[j].equals(o)) {
                    temp[i] = objects[j];
                    i++;
                }
            }
        }
        if(i > 0) {
            objects = temp;
            sort();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        objects = new FileSystemObject[0];
    }

    public void sort() {
        FileSystemObject[] temp = new FileSystemObject[objects.length];
        int i = 0;
        for(int j = 0; j < objects.length; j++) {
            if(objects[j] != null) {
                temp[i] = objects[j];
                i++;
            }
        }
        objects = new FileSystemObject[i];
        System.arraycopy(temp, 0, objects, 0, i);
        sort(objects, 0, size() - 1);
    }

    private static void sort(FileSystemObject[] array, int left, int right) {
        if(array.length == 0 || left == right) {
            return;
        }
        int mid = (left + right) / 2;
        sort(array, left, mid);
        sort(array, mid + 1, right);
        merge(array, left, mid, right);
    }

    private static void merge(FileSystemObject[] array, int left, int mid, int right) {
        int size = right - left + 1;
        FileSystemObject[] temp = new FileSystemObject[size];
        int i = left;
        int j = mid + 1;
        int k = 0;

        while(i <= mid && j <= right) {
            if(array[i].name.compareTo(array[j].name) < 0) {
                temp[k] = array[i];
                i++;
            } else {
                temp[k] = array[j];
                j++;
            }
            k++;
        }

        while(i <= mid) {
            temp[k] = array[i];
            i++;
            k++;
        }

        while(j <= right) {
            temp[k] = array[j];
            j++;
            k++;
        }

        for(k = 0; k < size; k++) {
            array[left + k] = temp[k];
        }
    }

    @Override
    public String toString() {
        if(isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size(); i++) {
            sb.append(objects[i]);
            sb.append(", ");
        }
        String out = sb.toString();
        return String.format("[%s]", out.substring(0, out.length() - 2));
    }
}
