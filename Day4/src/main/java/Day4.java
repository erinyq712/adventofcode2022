import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class Day4 {
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

    private record range(int start, int end) {
        public boolean inRange(range r) {
            return start >= r.start && end <= r.end;
        }

        public boolean isOverlapping(range range2) {
            return (end >= range2.start && start <= range2.start) || (range2.end >= start && range2.start <= start);
        }
    }

    public static final String RANGE_EXPRESSION = "((\\d+)-(\\d+))(,((\\d+)-(\\d+)))*";
    private static Pattern rangePattern = Pattern.compile(RANGE_EXPRESSION);
    private static void execise1(List<String> lines) {
        var count = lines.stream().mapToInt(line -> isFullyOverlapping(line)).sum();
        System.out.println("# overlapping: " + count);
    }

    private static int isFullyOverlapping(String line) {
        var matcher = rangePattern.matcher(line);
        if (matcher.matches()) {
            var range1 = new range(Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
            var range2 = new range(Integer.parseInt(matcher.group(6)), Integer.parseInt(matcher.group(7)));
            if (range1.inRange(range2) || range2.inRange(range1)) {
                if (range1.inRange(range2)) {
                    System.out.println(range1 + " is in " + range2);
                }
                if (range2.inRange(range1)) {
                    System.out.println(range2 + " is in " + range1);
                }
                return 1;
            }

        }
        return 0;
    }

    private static int isPartyOverlapping(String line) {
        var matcher = rangePattern.matcher(line);
        if (matcher.matches()) {
            var range1 = new range(Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
            var range2 = new range(Integer.parseInt(matcher.group(6)), Integer.parseInt(matcher.group(7)));
            if (range1.isOverlapping(range2) || range2.isOverlapping(range1)) {
                if (range1.isOverlapping(range2)) {
                    System.out.println(range1 + " is overlapping " + range2);
                }
                return 1;
            }

        }
        return 0;
    }

    private static void execise2(List<String> lines) {
        var count = lines.stream().mapToInt(line -> isPartyOverlapping(line)).sum();
        System.out.println("# overlapping: " + count);
    }
}
