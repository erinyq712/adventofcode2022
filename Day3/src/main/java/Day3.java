import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day3 {
    public static void main(String[] args) {
        try {
            var path = args.length > 0 ? Path.of(args[0]) : Path.of("input.txt");
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);

                // execise1(lines);
                execise2(lines);

            }
        } catch (IOException e) {
            // Unexpected
        }
    }

    private static void execise1(List<String> lines) {
        List<Items> items = lines.stream().map(Day3::getItems).collect(Collectors.toList());
        items.forEach(i -> {
            var priority = i.intersection().stream().map(Day3::getPriority).collect(Collectors.summingInt(Integer::intValue));
            System.out.println(setToString(i.intersection()) + ": " + priority);
        });
        var priorities = items.stream()
                .flatMap(i -> i.intersection().stream())
                .map(Day3::getPriority).collect(Collectors.summingInt(Integer::intValue));
        System.out.println(priorities);
    }

    private static Items getItems(String l) {
        int compartmentSize = (l.length()+1)/2;
        return new Items(l.substring(0, compartmentSize), l.substring(compartmentSize, l.length()));
    }

    private record Team(int team, String line) {

    }

    private static void execise2(List<String> lines) {
        var groups =
                IntStream.range(0,lines.size())
                    .mapToObj(i -> new Team(i/3, lines.get(i)))
                    .collect(Collectors.groupingBy(Team::team));
        var sum = groups.entrySet().stream().map(g -> {
            var badge = getBadge(g.getValue());
            var prio = getPriority(badge);
            System.out.println(g.getKey() + ": " + String.valueOf(badge) + ", " + prio);
            return prio;
        }).reduce(Integer::sum).orElse(0);
        System.out.println("Sum: " + sum);
    }

    private static char getBadge(List<Team> values) {
        var allItems = values.stream().map(s -> getSet(s.line())).collect(Collectors.toList());
        var result = allItems.stream().skip(1)
                .collect(() -> new HashSet<Character>(allItems.get(0)), HashSet::retainAll, HashSet::retainAll);
        return result.stream().findFirst().orElse((char)0);
    }

    private static Set<Character> getSet(String comp1) {
        var chars = comp1.chars().mapToObj(i->(char)i);
        return chars.collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    record Items(String comp1, String comp2) {
        public Set<Character> firstSet() {
            return getSet(comp1);
        }
        public Set<Character> secondSet() {
            return getSet(comp2);
        }
        public Set<Character> intersection() {
            var result = firstSet();
            result.retainAll(secondSet());
            return result;
        }
    }


    public static String setToString(Set<Character> set) {
        return set.stream().map(c -> String.valueOf(c)).collect(Collectors.joining());
    }

    private static int getPriority(char item) {
        if (item >= 'a' && item <= 'z') {
            return item-'a'+1;
        }
        if (item >= 'A' && item <= 'Z') {
            return item-'A'+27;
        }
        return 0;
    }
}
