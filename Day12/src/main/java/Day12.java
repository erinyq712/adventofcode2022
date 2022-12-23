import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day12 {
    private static Logger log = LoggerFactory.getLogger(Day12.class);

    public static void main(String[] args) {
        try {
            var path = args.length > 0 ? Path.of(args[0]) : Path.of("input.txt");
            if (Files.exists(path)) {
                String content = Files.readString(path);
                execise1(content);
                execise2(content);

            }
        } catch (IOException e) {
            // Unexpected
        }
    }
    record Position(int x, int y) {
    }

    static class HeightMap {
        private static Logger log = LoggerFactory.getLogger(HeightMap.class);

        private static final int START = 'S';
        private static final int END = 'E';
        private int width;
        private int height;
        private Position startPosition;
        private Position endPosition;

        private Map<Position,Integer> heights;
        private Map<Position,Integer> distances;
        private Set<Position> nonVisited;

        public  HeightMap(String content) {
            List<String> lines = Stream.of(content.split("\n")).toList();
            width = lines.get(0).length();
            height = lines.size();
            List<List<Integer>> heightData = lines.stream().map(this::stringToIntList).toList();
            int endPosY = content.indexOf(END) / (width+1);
            int endPosX = content.indexOf(END) % (width+1);
            int startPosY = content.indexOf(START) / (width+1);
            int startPosX = content.indexOf(START) % (width+1);
            this.heights = new HashMap<>();
            this.distances = new HashMap<>();
            this.nonVisited = new HashSet<>();
            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    if (x == endPosX && y == endPosY) {
                        endPosition = new Position(x, y);
                        heights.put(endPosition, (int)'z');
                        distances.put(endPosition, -1);
                        nonVisited.add(endPosition);
                    } else if (x == startPosX && y == startPosY) {
                        startPosition = new Position(x, y);
                        heights.put(startPosition, (int)'a');
                        distances.put(startPosition, 0);
                        nonVisited.add(startPosition);
                    } else {
                        var position = new Position(x, y);
                        heights.put(position, heightData.get(y).get(x));
                        distances.put(position, -1);
                        nonVisited.add(position);
                    }
                }
            }
        }

        public int steps() {
            return distances.get(endPosition);
        }

        public void printReachable() {
            var sorted = distances.entrySet().stream().sorted((p,q) -> {
                var result = Integer.compare(p.getKey().y, q.getKey().y);
                return result == 0 ? Integer.compare(p.getKey().x, q.getKey().x) : result;
            }).toList();
            var iterator = sorted.iterator();
            for(int i = 0; i < height; i++) {
                StringBuilder sb = new StringBuilder();
                for(int j = 0; j < width; j++) {
                    if (iterator.hasNext()) {
                        var next = iterator.next();
                        if (next.getValue().equals(-1)) {
                            sb.append("O");
                        } else {
                            sb.append("X");
                        }
                    } else {
                        throw new RuntimeException("Fxcked up");
                    }
                }
                System.out.println(sb.toString());
            }
        }

        public List<Position> findStartPositions() {
            return heights.entrySet().stream().filter(e -> e.getValue() == 'a').map(Map.Entry::getKey).toList();
        }

        public void resetDistances(Position position) {
            distances.entrySet().forEach(e -> e.setValue(-1));
            nonVisited = heights.keySet().stream().collect(Collectors.toSet());
            startPosition = position;
            distances.put(startPosition, 0);
        }

        static class Route {
            private List<Position> positions;
            private boolean isValid = true;
            private Set<Position> excluded;

            public Route() {
                this.positions = new ArrayList<>();
                this.excluded = new HashSet<>();
            }

            public Route(Route route, Position position) {
                this.positions = new ArrayList<>(route.positions);
                this.positions.add(position);
                this.excluded = new HashSet<>();
            }

            public void setValid(boolean valid) {
                isValid = valid;
            }

            public boolean isValid() {
                return isValid;
            }

            public void addExcluded(Position p) {
                excluded.add(p);
            }

            public boolean isExcluded(Position p) {
                return excluded.contains(p);
            }

            public Position last() {
                return positions.get(positions.size()-1);
            }

            public void push(Position p) {
                positions.add(p);
            }

            public void pop() {
                positions.remove(positions.size()-1);
            }

            @Override
            public String toString() {
                return "Route{" +
                        "positions=" + positions.size() +
                        '}';
            }
        }

        private static boolean isAcceptable(int next, int previous) {
            return next-previous<=1 || previous>next;
        }

        private List<Integer> stringToIntList(String s) {
            return s.chars().collect(ArrayList<Integer>::new, ArrayList<Integer>::add, ArrayList<Integer>::addAll);
        }

        public void measureRoutes() {
            measureRoutes(startPosition);
        }

        private Optional<Route> findBestRoute() {
            var next = findBestRoute(endPosition, distances.get(endPosition), new Route());
            if (next.isValid) {
                return Optional.of(next);
            } else {
                return Optional.empty();
            }
        }

        private Route findBestRoute(Position position, Integer distance, Route route) {
            route.push(position);
            var result = findBestRoute(distance, 1, route);
            if (! result.isValid()) {
                route.setValid(true);
                return findBestRoute(distance, 1, route);
            }
            return result;
        }

        private Route findBestRoute(int distance, int diff, Route route) {
            if (route.last().equals(startPosition)) {
                return route;
            }
            var next = getNext(route.last(), distance, diff, route);
            if (next.isPresent()) {
                var position = next.get();
                route.push(position);
                var result = findBestRoute(distance - 1, diff, route);
                if (! result.isValid()) {
                    if (route.last() == position) {
                        route.pop();
                        route.addExcluded(next.get());
                    }
                    return findBestRoute(distance, diff, route);
                }
                return result;
            } else {
                route.setValid(false);
            }
            return route;
        }

        private Optional<Route> findShortestRoute(List<Route> routes) {
            if (routes.isEmpty()) {
                return Optional.empty();
            } else {
                return routes.stream().sorted(Comparator.comparingInt(r -> r.positions.size())).findFirst();
            }
        }

        private Optional<Position> getNext(Position position, int distance, int diff, Route route) {
            if (position.x > 0) {
                var next = new Position(position.x-1, position.y);
                if (! route.isExcluded(next) && distances.get(next) + diff == distance) {
                    return Optional.of(next);
                }
            }
            if (position.x < width-1) {
                var next = new Position(position.x+1, position.y);
                if (! route.isExcluded(next) && distances.get(next) + diff == distance) {
                    return Optional.of(next);
                }
            }
            if (position.y > 0) {
                var next = new Position(position.x, position.y-1);
                if (! route.isExcluded(next) && distances.get(next) + diff == distance) {
                    return Optional.of(next);
                }
            }
            if (position.y < height-1) {
                var next = new Position(position.x, position.y+1);
                if (! route.isExcluded(next) && distances.get(next) + diff == distance) {
                    return Optional.of(next);
                }
            }
            return Optional.empty();
        }

        public void measureRoutes(Position position) {
            nonVisited.remove(position);
            if (position != endPosition) {
                getAdjacent(position).forEach(a -> {
                    if (nonVisited.contains(a)) {
                        measureRoutes(a);
                    }
                });
            }
        }

        private void setDistance(Position position, int distance) {
            var current = distances.get(position);
            if (current > distance || current == -1) {
                distances.put(position,distance);
                log.debug("Setting distance for {} = {}", position, distance);
                getAdjacent(position).forEach(a -> setDistance(a,distance+1));
            }
        }

        private void update(Position position, List<Position> adjacent, Position previous) {
            log.debug("Adding {} from {}", position, previous);
            var distance = distances.get(previous) + 1;
            var acceptable = isAcceptable(heights.get(position), heights.get(previous));
            if (acceptable) {
                setDistance(position, distance);
                adjacent.add(position);
            } else {
                log.debug("Not acceptable from {}:{} to {}:{}", previous, heights.get(previous), position, heights.get(position));
            }
        }

        private List<Position> getAdjacent(Position position) {
            List<Position> adjacent = new ArrayList<>();
            if (position.x > 0) {
                update(new Position(position.x-1, position.y), adjacent, position);
            }
            if (position.x < width-1) {
                update(new Position(position.x+1, position.y), adjacent, position);
            }
            if (position.y > 0) {
                update(new Position(position.x, position.y-1), adjacent, position);
            }
            if (position.y < height-1) {
                update(new Position(position.x, position.y+1), adjacent, position);
            }
            return adjacent;
        }

    }

    private static void execise1(String content) {
        HeightMap map = new HeightMap(content);
        map.measureRoutes();
        // To understand problem with not covering full map
        // map.printReachable();
        log.info("Number of steps: {}", map.steps());
        var route = map.findBestRoute();
        if (route.isPresent()) {
            route.get().positions.stream().map(Position::toString).forEach(log::debug);
        }
    }

    private static void execise2(String content) {
        HeightMap map = new HeightMap(content);
        var startPositions = map.findStartPositions();
        var routes = startPositions.stream().map(position -> {
            map.resetDistances(position);
            map.measureRoutes(position);
            return map.findBestRoute().orElse(null);
        }).filter(Objects::nonNull).toList();
        var route = map.findShortestRoute(routes);
        if (route.isPresent()) {
            route.get().positions.stream().map(Position::toString).forEach(log::debug);
            log.info("Number of steps in route: {}", route.get().positions.size()-1);
        }


    }

}