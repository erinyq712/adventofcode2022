import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Day1 {
    public static void main(String[] args) {
        try {
            var path = args.length > 0 ? Path.of(args[0]) : Path.of("input.txt");
            if (Files.exists(path)) {
                List<Long> lines = Files.readAllLines(path).stream().mapToLong(Long::valueOf).sorted()
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

    private static void execise1(List<Long> lines) {
        if (lines.size()<2) return;
        List<Match> matches = new ArrayList<>();
        var current = 0;
        var last = lines.size() - 1;
        while (current < last) {
            var sum = lines.get(current) + lines.get(last);
            while (sum >= 2020 && last > current) {
                if (sum == 2020) {
                    matches.add(new Match(lines.get(current), lines.get(last)));
                }
                last--;
                sum = lines.get(current) + lines.get(last);
            }
            current++;
            last = lines.size() - 1;
        }
        matches.forEach(m -> System.out.println("(" + m.n1 + "," + m.n2 + ") -> " + (m.n1*m.n2)));
    }

    private static void execise2(List<Long> lines) {
        if (lines.size()<3) return;
        List<Match3> matches = new ArrayList<>();
        var current = 0;
        var last = lines.size() - 1;
        var secondlast = last-1;
        while (current < secondlast && secondlast < last) {
            var sum = lines.get(current) + lines.get(secondlast) + lines.get(last);
            while (sum >= 2020 && last > secondlast && secondlast > current) {
                while (sum >= 2020 && secondlast > current) {
                    if (sum == 2020) {
                        matches.add(new Match3(lines.get(current), lines.get(secondlast), lines.get(last)));
                    }
                    secondlast--;
                    sum = lines.get(current) + lines.get(secondlast) + lines.get(last);
                }
                last--;
                secondlast = last-1;
                sum = lines.get(current) + lines.get(secondlast) + lines.get(last);
            }
            current++;
            last = lines.size() - 1;
            secondlast = last-1;
        }
        matches.forEach(m -> System.out.println("(" + m.n1 + "," + m.n2 + "," + m.n3 + ") -> " + (m.n1*m.n2*m.n3)));
    }

    private record Match(long n1, long n2) {
    }

    private record Match3(long n1, long n2, long n3) {
    }
}
