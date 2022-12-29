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
                execise2(lines);

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
        ROCK,
        SAND,
        AIR
    }

    static class CaveMap {
        private Map<Position, ItemType> items;
        protected Position topLeft;
        protected Position downRight;

        public CaveMap(Map<Position, ItemType> caveMap) {
            items = caveMap;
            int xmin = caveMap.keySet().stream().map(Position::x).min(Integer::compareTo).orElse(0);
            int xmax = caveMap.keySet().stream().map(Position::x).max(Integer::compareTo).orElse(0);
            int ymin = caveMap.keySet().stream().map(Position::y).min(Integer::compareTo).orElse(0);
            int ymax = caveMap.keySet().stream().map(Position::y).max(Integer::compareTo).orElse(0);
            topLeft = new Position(xmin, ymin);
            downRight = new Position(xmax, ymax);
        }

        public void put(Position p, ItemType t) {
            items.put(p,t);
        }

        public ItemType findItemType(Position p) {
            if (items.containsKey(p)) {
                return items.get(p);
            } else {
                return ItemType.AIR;
            }
        }

        public boolean isInside(Position grainPosition) {
            return grainPosition.x() >= topLeft.x() &&
                    grainPosition.x() <= downRight.x() &&
                    grainPosition.y() <= downRight.y();
        }

        public long count(ItemType type) {
            return items.values().stream().filter(it -> it == type).count();
        }

        public boolean isAvailable(Position nextGrainPosition) {
            var itemType = findItemType(nextGrainPosition);
            return itemType == ItemType.AIR;
        }
    }

    static class CaveMapWithFloor extends CaveMap {

        public CaveMapWithFloor(Map<Position, ItemType> caveMap) {
            super(caveMap);

        }

        @Override
        public boolean isInside(Position grainPosition) {
            return grainPosition.y() <= downRight.y() + 1;
        }

        @Override
        public boolean isAvailable(Position nextGrainPosition) {
            if (! isInside(nextGrainPosition)) {
                return false;
            }
            return super.isAvailable(nextGrainPosition);
        }
    }

    private static String regex = "(\\d{1,3},\\d{1,3})";
    private static Pattern positionsPattern = Pattern.compile(regex);

    private static boolean checkIfPositionIsAvailable(CaveMap map, Position nextGrainPosition) {
        return map.isAvailable(nextGrainPosition);
    }

    private static Position processRange(Map<Position, ItemType> map, Position prev, String range) {
        var current = newPosition(range);
        map.put(current, ItemType.ROCK);
        if (prev != null) {
            var diff = current.subtract(prev);
            if (diff.x == 0) {
                while (! prev.equals(current)) {
                    prev = prev.add(getNextY(diff));
                    map.put(prev, ItemType.ROCK);
                }
            } else if (diff.y == 0) {
                while (! prev.equals(current)) {
                    prev = prev.add(getNextX(diff));
                    map.put(prev, ItemType.ROCK);
                }
            }
            assert(prev.equals(current));
        }
        return current;
    }

    private static Position getNextX(Position diff) {
        return diff.x() > 0 ? ONE_RIGHT : ONE_LEFT;
    }

    private static Position getNextY(Position diff) {
        return diff.y() > 0 ? ONE_UP : ONE_DOWN;
    }

    private static Map<Position, ItemType> createCaveMap(List<String> lines) {
        Map<Position, ItemType> map = new HashMap<>();
        lines.forEach(line -> {
            var matcher = positionsPattern.matcher(line);
            Position prev = null;
            while (matcher.find()) {
                var range = matcher.group();
                prev = processRange(map, prev, range);
            }
        });
        return map;
    }

    private static void processSand(CaveMap map) {
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
                map.put(grainPosition, ItemType.SAND);
                done = grainPosition == START_POSITION;
            } else {
                done = true;
            }
        }
    }

    private static void execise1(List<String> lines) {
        CaveMap map = new CaveMap(createCaveMap(lines));
        processSand(map);
        var result = map.count(ItemType.SAND);
        log.info("Result: {}", result);
    }

    private static void execise2(List<String> lines) {
        CaveMapWithFloor map = new CaveMapWithFloor(createCaveMap(lines));
        processSand(map);

        var result = map.count(ItemType.SAND);
        log.info("Result: {}", result);
    }
}