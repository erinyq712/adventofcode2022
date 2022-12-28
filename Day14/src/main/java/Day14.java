import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Day14 {

    private static Logger log = LoggerFactory.getLogger(Day14.class);

    private static final Position ONE_UP = new Position(0,1);
    private static final Position ONE_DOWN = new Position(0,-1);
    private static final Position ONE_RIGHT = new Position(1,0);
    private static final Position TWO_RIGHT = new Position(2,0);
    private static final Position ONE_LEFT = new Position(-1,0);
    public static final Position START_POSITION = new Position(500, 0);

    public static void main(String[] args) {
        try {
            var path = args.length > 0 ? Path.of(args[0]) : Path.of("input.txt");
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path).stream()
                        .collect(ArrayList::new,
                                ArrayList::add,
                                ArrayList::addAll);
                execise1(lines);
                // execise2(lines);

            }
        } catch (IOException e) {
            // Unexpected
        }
    }

    record Position(int x, int y) {

        public Position add(Position p) {
            return new Position(x+p.x, y+p.y);
        }

        public Position subtract(Position p) {
            return new Position(x-p.x, y-p.y);
        }

        public Position abs() {
            return new Position(Math.abs(x), Math.abs(y));
        }
    }

    static Position newPosition(String range) {
        int[] coords = Arrays.stream(range.split(",")).mapToInt(Integer::parseInt).toArray();
        return new Position(coords[0], coords[1]);
    }

    enum ItemType {
        Rock,
        Sand,
        Air
    }

    static class CaveMap {
        private Map<Position, ItemType> items;
        private Position topLeft;
        private Position downRight;

        public CaveMap() {
            items = new HashMap<>();
            topLeft = START_POSITION;
            downRight = START_POSITION;
        }

        public void put(Position p, ItemType t) {
            items.put(p,t);
            if (p.x() < topLeft.x()) {
                topLeft = new Position(p.x(), topLeft.y());
            }
            if (p.x() > downRight.x()) {
                if (p.y() > downRight.y()) {
                    downRight = new Position(p.x(), p.y());
                } else {
                    downRight = new Position(p.x(), downRight.y());
                }
            } else if (p.y() > downRight.y()) {
                downRight = new Position(p.x(), downRight.y());
            }
        }

        public ItemType findItemType(Position p) {
            if (items.containsKey(p)) {
                return items.get(p);
            } else {
                return ItemType.Air;
            }
        }

        public boolean isInside(Position grainPosition) {
            return grainPosition.x() >= topLeft.x() &&
                    grainPosition.x() <= downRight.x() &&
                    grainPosition.y() <= downRight.y();
        }

        public long count(ItemType sand) {
            return items.values().stream().filter(it -> it == ItemType.Sand).count();
        }
    }

    private static String regex = "(\\d{1,3},\\d{1,3})";
    private static Pattern positionsPattern = Pattern.compile(regex);

    private static void execise1(List<String> lines) {
        CaveMap map = new CaveMap();
        lines.forEach(line -> {
            var matcher = positionsPattern.matcher(line);
            Position prev = null;
            while (matcher.find()) {
                var range = matcher.group();
                prev = processRange(map, prev, range);
            }
        });
        boolean done = false;
        while (! done) {
            Position grainPosition = START_POSITION;
            boolean canMove = true;
            while (canMove && map.isInside(grainPosition)) {
                var newPosition = grainPosition.add(ONE_UP);
                canMove = checkIfPositionIsAvailable(map, newPosition);
                if (canMove) {
                    grainPosition = newPosition;
                } else {
                    newPosition = newPosition.add(ONE_LEFT);
                    canMove = checkIfPositionIsAvailable(map, newPosition);
                    if (canMove) {
                        grainPosition = newPosition;
                    } else {
                        newPosition = newPosition.add(TWO_RIGHT);
                        canMove = checkIfPositionIsAvailable(map, newPosition);
                        if (canMove) {
                            grainPosition = newPosition;
                        }
                    }
                }
            }
            if (map.isInside(grainPosition)) {
                map.put(grainPosition, ItemType.Sand);
            } else {
                done = true;
            }
        }
        var result = map.count(ItemType.Sand);
        log.info("Result: {}", result);
    }

    private static boolean checkIfPositionIsAvailable(CaveMap map, Position nextGrainPosition) {
        var itemType = map.findItemType(nextGrainPosition);
        return itemType == ItemType.Air;
    }

    private static Position processRange(CaveMap map, Position prev, String range) {
        var current = newPosition(range);
        map.put(current, ItemType.Rock);
        if (prev != null) {
            var diff = current.subtract(prev);
            if (diff.x == 0) {
                while (! prev.equals(current)) {
                    prev = prev.add(diff.y() > 0 ? ONE_UP : ONE_DOWN);
                    map.put(prev, ItemType.Rock);
                }
            } else if (diff.y == 0) {
                while (! prev.equals(current)) {
                    prev = prev.add(diff.x() > 0 ? ONE_RIGHT : ONE_LEFT);
                    map.put(prev, ItemType.Rock);
                }
            }
            assert(prev.equals(current));
        }
        return current;
    }

//    private static void execise2(List<String> lines) {
//
//    }
}