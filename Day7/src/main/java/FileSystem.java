import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystem {

    private Directory root;
    private Directory currentDirectory;

    public FileSystem() {
        root = currentDirectory = new Directory("/");
    }

    record File(String name, long size) {
    }

    class Directory {
        private String name;
        private Directory parent;
        private List<Directory> subdirs;
        private List<File> files;

        public Directory(String name) {
            this(name, null);
        }

        public Directory(String name, Directory parent) {
            this.name = name;
            this.parent = parent;
            subdirs = new ArrayList<>();
            files = new ArrayList<>();
        }

        public boolean hasParent() {
            return parent != null;
        }

        public Directory getParent() {
            if (parent == null) {
                throw new RuntimeException("Root directory has no parent");
            }
            return parent;
        }

        private Directory getDirectory(String name) {
            return subdirs.stream().filter(dir -> dir.name.equals(name)).findFirst()
                    .orElseGet(() -> addDirectory(name));
        }

        private Directory addDirectory(String name) {
            var dir = new Directory(name, this);
            subdirs.add(dir);
            return dir;
        }

        public void addFile(String name, long size) {
            files.add(new File(name, size));
        }

        public Directory dir(String name) {
            return getDirectory(name);
        }

        public List<DirectorySize> sizes() {
            var fileSizes = files.stream().map(File::size).reduce(Long::sum).orElse(0L);
            var subResults = subdirs.stream().flatMap(s -> s.sizes().stream()).collect(Collectors.toList());
            // Must only sum sizes of sub-directories
            var directorySizes = subResults.stream().filter(sr -> sr.directory.parent == this)
                    .map(ss -> ss.size()).reduce(Long::sum).orElse(0L);
            var current = new DirectorySize(this, fileSizes + directorySizes);
            return Stream.concat(Stream.of(current), subResults.stream()).collect(Collectors.toList());
        }

        public long fileSizes() {
            return files.stream().map(File::size).reduce(Long::sum).orElse(0L);
        }

        public int level() {
            return parent != null ? parent.level() + 1 : 0;
        }

        public List<File> getFiles() {
            return files;
        }
    }

    public void cd(String name) {
        if (name.equals("..") && currentDirectory.hasParent()) {
            currentDirectory = currentDirectory.getParent();
        } else if (name.equals("/")) {
            currentDirectory = root;
        } else {
            currentDirectory = currentDirectory.getDirectory(name);
        }
    }

    public void file(String name, long size) {
        currentDirectory.addFile(name, size);
    }

    public Directory dir(String name) {
        return currentDirectory.dir(name);
    }

    public record DirectorySize (Directory directory, long size) {
        @Override
        public String toString() {
            return directory.name + ": " + size;
        }
    }

    public List<DirectorySize> sizes() {
        return root.sizes();
    }
}
