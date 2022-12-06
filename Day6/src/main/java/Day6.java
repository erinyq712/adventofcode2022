import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

public class Day6 {
    public static void main(String[] args) {
        try {
            var path = args.length > 0 ? Path.of(args[0]) : Path.of("input.txt");
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);

                execise1(lines);
                execise2(lines);

            }
        } catch (IOException e) {
            // Unexpected
        }
    }

    private static void execise1(List<String> lines) {
        lines.forEach(line -> {
            int start = 0;
            int end = 4;
            while (end < line.length() && ! isMarker(line.substring(start, end))) {
                start++;
                end++;
            }
            if (end < line.length() && isMarker(line.substring(start, end))) {
                System.out.println(start+4);
            }
        });
    }

    private static boolean isMarker(String substring) {
        var charSet = substring.chars().collect(HashSet::new, HashSet::add, HashSet::addAll );
        return charSet.size() == substring.length();
    }

    private static void execise2(List<String> lines) {
        lines.forEach(line -> {
            int start = 0;
            int end = 14;
            while (end < line.length() && ! isMarker(line.substring(start, end))) {
                start++;
                end++;
            }
            if (end < line.length() && isMarker(line.substring(start, end))) {
                System.out.println(start+14);
            }
        });
    }
}
