import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day5 {
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

    private static Pattern movePattern = Pattern.compile("move (\\d+) from (\\d+) to (\\d+)");

    private record Action(int moveCount, int fromColumn, int toColumn) {

    }

    private static void execise1(List<String> lines) {
        var config = lines.stream().takeWhile(l->l.length()>0).map(l -> l.split("(?<=\\G.{4})")).collect(Collectors.toUnmodifiableList());
        var numberOfColumns = config.get(config.size()-1).length;
        var initialColumns = IntStream.range(0, numberOfColumns).mapToObj(i -> {
            return IntStream.range(0, config.size()-1).mapToObj(j -> {
                if (config.get(j).length > i) {
                    var box = config.get(j)[i];
                    return box.startsWith(" ") ? null : box;
                } else return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }).collect(Collectors.toList());
        System.out.println(numberOfColumns);
        var moves = lines.subList(config.size()+1,lines.size()).stream().map(l -> {
            var matcher = movePattern.matcher(l);
            if (matcher.matches()) {
                return new Action(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
            } else {
                throw new RuntimeException("Regexp error");
            }
        }).collect(Collectors.toList());
        var iterator = moves.iterator();
        var columns = initialColumns;
        while (iterator.hasNext()) {
            var m = iterator.next();
            var newConfig = new ArrayList<List<String>>(columns.size());
            for(int i = 0; i<columns.size(); i++) {
                if (i == m.fromColumn-1) {
                    newConfig.add(columns.get(i).subList(m.moveCount, columns.get(i).size()));
                } else if (i == m.toColumn-1) {
                    var added = new ArrayList<>(columns.get(m.fromColumn-1).subList(0, m.moveCount));
                    Collections.reverse(added);
                    newConfig.add(Stream.concat(added.stream(), columns.get(i).stream()).collect(Collectors.toList()));
                } else {
                    newConfig.add(columns.get(i));
                }
            }
            columns = newConfig;
        };
        System.out.println(columns.stream().map(c -> c.get(0).substring(1,2)).collect(Collectors.joining()));
    }


    private static void execise2(List<String> lines) {
    }
}
