import erinyq.Position;
import erinyq.Range;
import erinyq.Ranges;
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
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class Day15 {
    private static Logger log = LoggerFactory.getLogger(Day15.class);

    public static void main(String[] args) {
        try {
            var path = args.length > 0 ? Path.of(args[0]) : Path.of("input.txt");
            long max = args.length > 1 ? Long.parseLong(args[1]): 4000000;
            // var path = args.length > 0 ? Path.of(args[0]) : Path.of("inputtest.txt");
            // long max = args.length > 1 ? Long.parseLong(args[1]): 20;
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path).stream()
                        .collect(ArrayList::new,
                                ArrayList::add,
                                ArrayList::addAll);
                execise1(max, lines);
                execise2(max, lines);

            }
        } catch (IOException e) {
            // Unexpected
        }
    }

    enum ItemType {
        SENSOR,
        BEACON,
        AIR
    }

    static final String ROWREGEX = "Sensor at x=([^,]+), y=([^:]+): closest beacon is at x=([^,]+), y=([^$]+)";
    static final Pattern rowPattern = Pattern.compile(ROWREGEX);
    private static void execise1(final long line, List<String> lines) {
        Map<Position, Position> sensors = new HashMap<>();
        Set<Position> beacons = new HashSet<>();
        parseInput(lines, sensors, beacons);

        var sensorPositions = getSensorPositions(line, sensors);
        Ranges ranges = new Ranges(sensorPositions);
        var positions = ranges.getRanges().stream().mapToLong(r -> r.endExclusive()-r.start()).sum()-beacons.stream().filter(r->r.y()==line).count();
        log.info("Number of non-beacon spaces: {}", positions);
    }

    private static List<Range> getSensorPositions(long line, Map<Position, Position> sensors) {
        return sensors.entrySet().stream().filter(e -> isInRange(line, e)).map(sp -> {
            var ydiff = abs(sp.getKey().y() - line);
            var xdiff = sp.getValue().length() - ydiff;
            var start = sp.getKey().x() - xdiff;
            var endExclusive = sp.getKey().x() + xdiff + 1;
            return new Range(start, endExclusive);
        }).toList();
    }

    private static List<Range> getSensorPositions(long line, Map<Position, Position> sensors, long max) {
        return sensors.entrySet().stream().filter(e -> isInRange(line, e)).map(sp -> {
            var ydiff = abs(sp.getKey().y() - line);
            var xdiff = sp.getValue().length() - ydiff;
            var start = max(0,sp.getKey().x() - xdiff);
            var endExclusive = min(max, sp.getKey().x() + xdiff + 1);
            return start < endExclusive ? new Range(start, endExclusive) : null;
        }).filter(Objects::nonNull).toList();
    }
    private static void parseInput(List<String> lines, Map<Position, Position> sensors, Set<Position> beacons) {
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
    }

    private static boolean isInRange(long line, Map.Entry<Position, Position> e) {
        return abs(e.getKey().y()-line) <= e.getValue().length();
    }

    private static boolean isInRange(Position p, Map.Entry<Position, Position> e) {
        var diff = e.getKey().subtract(p);
        var distance = diff.length();
        return e.getValue().length() >= distance;
    }

    private static Position createPosition(long x, long y) {
        return new Position(x,y);
    }

    private static void execise2(long max, List<String> lines) {
        final Map<Position, Position> sensors = new HashMap<>();
        final Set<Position> beacons = new HashSet<>();
        parseInput(lines, sensors, beacons);

        var allRanges = new HashMap<Long, List<Range>>();
        for(long y = 0; y < max; y++) {
            final long fy = y;
            var ranges = new Ranges();
            var sensorPositions = getSensorPositions(fy, sensors, max);
            ranges.addAll(sensorPositions);
            if (ranges.getRanges().size() > 1) {
                log.debug("Ranges found: {}", y);
            }
            var diff = ranges.difference();
            var union = diff.intersection(new Range(0L, max));
            if (! union.getRanges().isEmpty()) {
                allRanges.put(y, union.getRanges());
            }
        }
        var result = allRanges.entrySet().stream().filter(es -> ! es.getValue().isEmpty()).toList();
        if (result.size() == 1) {
            var range = result.get(0);
            var signal = range.getValue().get(0).start() * 4000000 + range.getKey();
            log.info("Result: {}", signal);
        }
    }
}
