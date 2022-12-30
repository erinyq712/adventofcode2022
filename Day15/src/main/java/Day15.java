import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.LongStream;

import static java.lang.Math.abs;

public class Day15 {
    private static Logger log = LoggerFactory.getLogger(Day15.class);

    public static void main(String[] args) {
        try {
            var path = args.length > 0 ? Path.of(args[0]) : Path.of("input.txt");
            long line = args.length > 1 ? Long.parseLong(args[1]): 2000000;

            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path).stream()
                        .collect(ArrayList::new,
                                ArrayList::add,
                                ArrayList::addAll);
                execise1(line, lines);
                execise2(lines);

            }
        } catch (IOException e) {
            // Unexpected
        }
    }

    record Position(long x, long y) {

        public Position add(Position p) {
            return new Position(x+p.x, y+p.y);
        }

        public Position subtract(Position p) {
            return new Position(x-p.x, y-p.y);
        }

        public Position abs() {
            return new Position(Math.abs(x), Math.abs(y));
        }

        public long length() {
            var a = abs();
            return a.x + a.y;
        }
    }

    enum ItemType {
        SENSOR,
        BEACON,
        AIR
    }

    static final String ROWREGEX = "Sensor at x=([^,]+), y=([^:]+): closest beacon is at x=([^,]+), y=([^$]+)";
    static final Pattern rowPattern = Pattern.compile(ROWREGEX);
    private static void execise1(long line, List<String> lines) {
        Map<Position, Position> sensors = new HashMap<>();
        Set<Position> beacons = new HashSet<>();
        lines.stream().forEach(l -> {
            var matcher = rowPattern.matcher(l);
            if (matcher.matches()) {
                var xSensor = Long.parseLong(matcher.group(1));
                var ySensor = Long.parseLong(matcher.group(2));
                var xBeacon = Long.parseLong(matcher.group(3));
                var yBeacon = Long.parseLong(matcher.group(4));
                var sensorPosition = createPosition(xSensor, ySensor);
                var beaconPosition = createPosition(xBeacon, yBeacon);
                var delta = sensorPosition.subtract(beaconPosition);
                sensors.put(sensorPosition, delta.abs());
                beacons.add(beaconPosition);
            }
        });
        long xmin = sensors.entrySet().stream().map(p -> p.getKey().x() - p.getValue().length()).min(Long::compareTo).orElse(0L);
        long xmax = sensors.entrySet().stream().map(p -> p.getKey().x() + p.getValue().length()).max(Long::compareTo).orElse(0L);

        // Get sensors in range
        var sensorPositions = sensors.entrySet().stream().filter(e -> isInRange(line,e)).toList();
        // Get crossing items
        long[] found = LongStream.range(xmin,xmax+1).filter(i -> isNotBeacon(sensorPositions.stream().toList(), beacons, new Position(i,line))).toArray();

        log.info("Number of non-beacon spaces: {}", found.length);
    }

    private static boolean isNotBeacon(List<Map.Entry<Position, Position>> sensorPositions, Set<Position> beacons, Position position) {
        return sensorPositions.stream().anyMatch(e -> isInRange(position, e)) && ! beacons.contains(position);
    }

    private static boolean isInRange(long line, Map.Entry<Position, Position> e) {
        return e.getKey().y()-line <= e.getValue().length();
    }

    private static boolean isInRange(Position p, Map.Entry<Position, Position> e) {
        var diff = e.getKey().subtract(p);
        var distance = diff.length();
        return e.getValue().length() >= distance;
    }

    private static Position createPosition(long x, long y) {
        return new Position(x,y);
    }

    private static void execise2(List<String> lines) {

    }
}
