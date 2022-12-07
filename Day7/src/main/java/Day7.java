import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Day7 {
    public static void main(String[] args) {
        try {
            var path = args.length > 0 ? Path.of(args[0]) : Path.of("input.txt");
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path).stream()
                        .collect(ArrayList::new,
                                ArrayList::add,
                                ArrayList::addAll);
                execise1(lines);
                execise2(lines);

            }
        } catch (IOException e) {
            // Unexpected
        }
    }

    private static Pattern commandPattern = Pattern.compile("(cd|ls)( (.*))?");
    private static Pattern filePattern = Pattern.compile("(dir|\\d+) (.*)");

    private static void execise1(List<String> lines) {
        FileSystem fs = new FileSystem();
        var iterator = lines.iterator();
        boolean readingFiles = false;
        while (iterator.hasNext()) {
            var line = iterator.next();
            boolean isCommand = line.startsWith("$");
            if (isCommand) {
                readingFiles = false;
                var matcher = commandPattern.matcher(line);
                if (matcher.find()) {
                    var command = matcher.group(1);
                    if (command.equals("cd")) {
                        var argument = matcher.group(3);
                        fs.cd(argument);
                    } else if (command.equals("ls")) {
                        readingFiles = true;
                    }
                }
            } else if (readingFiles) {
                var fileMatcher = filePattern.matcher(line);
                if (fileMatcher.find()) {
                    var spec = fileMatcher.group(1);
                    var name = fileMatcher.group(2);
                    if ("dir".equals(spec)) {
                        fs.dir(name);
                    } else {
                        var size = Long.parseLong(spec);
                        fs.file(name, size);
                    }
                }
            }
        }
        //
        fs.sizes().stream()
                .filter(ds -> ds.size() <= 100000)
                .forEach(ds -> {
                    var indent = " ".repeat(ds.directory().level());
                    System.out.println(indent + ds);
//            ds.directory().getFiles().stream().forEach(fss -> {
//                var fssindent = indent + "- ";
//                System.out.println(fssindent + fss);
//            });
        });
        var result = fs.sizes().stream().filter(ds -> ds.size() <= 100000).map(FileSystem.DirectorySize::size).reduce(Long::sum).orElse(0L);
        System.out.println(result);
    }

    private static void execise2(List<String> lines) {
        
    }
}
