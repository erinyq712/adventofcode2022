import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.util.Collections.unmodifiableList;

public class Failure12 {
    public static void main(String[] args) {
        try {
            var path = args.length > 0 ? Path.of(args[0]) : Path.of("input.txt");
            if (Files.exists(path)) {
                String content = Files.readString(path);
                execise1(content);
                //execise2(content);

            }
        } catch (IOException e) {
            // Unexpected
        }
    }

    static class Position implements Comparable<Position> {
        public int x;
        public int y;
        public int height;
        public int distance;

        public int visited;

        public int cost;

        Position(int x, int y, int height, int distance) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.distance = distance;
            this.cost = -1;
            this.visited = -1;
        }

        public boolean setCost(int cost) {
            if (this.cost > 0 && cost < this.cost) {
                this.cost = cost;
            } else if (this.cost < 0){
                this.cost = cost;
            } else {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(Position o) {
            return Integer.compare(this.distance, o.distance);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return x == position.x && y == position.y && height == position.height && distance == position.distance;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, height, distance);
        }

        @Override
        public String toString() {
            return "Position{" +
                    "x=" + x +
                    ", y=" + y +
                    ", height=" + height +
                    ", distance=" + distance +
                    ", visited=" + visited +
                    '}';
        }

        public void setVisited(int distance) {
            this.visited = distance;
        }

        public boolean isVisited(int distance) {
            return this.visited <= distance && this.visited > -1;
        }
    };

    static class HeightMap {
        private static final int START = (int)'S';
        private static final int MIN = (int)'a';
        private static final int END = (int)'E';
        private int width;
        private int height;
        private final String content;
        private List<List<Integer>> heights;
        private Position startPosition;
        private Position endPosition;

        private Map<Integer, List<Position>> sorted;

        public  HeightMap(String content) {
            List<String> lines = Stream.of(content.split("\n")).toList();
            width = lines.get(0).length();
            height = lines.size();
            this.content = content;
            heights = lines.stream().map(this::stringToIntList).toList();
            createEndPosition();
            createStartPosition();
            List<Position> nodes = new ArrayList<>();
            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    nodes.add(new Position(x, y, getHeight(x, y), getDistance(x, y)));
                }
            }
            sorted = nodes.stream().collect(Collectors.groupingBy(p -> p.distance));
        }

        record Route(List<Position> positions) {
            public Route() {
                this(new ArrayList<>());
            }

            public boolean atPosition(Position p) {
                return positions.stream().anyMatch(p2 -> p2.equals(p));
            }

            public boolean isEmpty() {
                return positions.isEmpty();
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

            public boolean isAdjacent(Position q) {
                return positions.stream().anyMatch(p2 -> abs(p2.height-q.height)<=1 && HeightMap.isAdjacent(p2, q));
            }
        }

        private static boolean isAdjacent(Position p, Position q) {
            if (p.x == q.x) {
                return abs(p.y-q.y) == 1;
            }
            if (p.y == q.y) {
                return abs(p.x-q.x) == 1;
            }
            return false;
        }

        private static boolean isAcceptable(Position p, Position q) {
            var heightOK = abs(p.height-q.height)<=1;
            return isAdjacent(p,q) && heightOK;
        }

        private void createStartPosition() {
            int pos = content.indexOf("S");
            int y = pos / (width+1);
            int x = pos % (width+1);
            startPosition = new Position(x,y, 'a', getDistance(x,y, endPosition));
            heights.get(y).set(x, startPosition.height);
        }

        private void createEndPosition() {
            int pos = content.indexOf("E");
            int y = pos / (width+1);
            int x = pos % (width+1);
            endPosition = new Position(x,y, 'z', 0);
            heights.get(y).set(x, endPosition.height);
        }

        private List<Integer> stringToIntList(String s) {
            return s.chars().collect(ArrayList<Integer>::new, ArrayList<Integer>::add, ArrayList<Integer>::addAll);
        }

        private int getHeight(int x, int y) {
            return heights.get(y).get(x);
        }

        private int getDistance(int x, int y, Position p2) {
            return abs(x-p2.x) + abs(y-p2.y);
        }

        private int getDistance(int x, int y) {
            return getDistance(x,y, endPosition);
        }

        List<Position> appendOne(List<Position> l, Position p) {
            List<Position> result = new ArrayList<Position>(l);
            result.add(p);
            return unmodifiableList(result);
        }

        public List<Position> findRoute(int depth, Position start) {
            if (start.equals(startPosition)) {
                return List.of(start);
            }
            int current = start.distance;
            Map<Position, Route> newRoutes = new HashMap<>();

            var currentPositions = Stream.concat(
                    Optional.ofNullable(sorted.get(current + 1)).map(s -> s.stream().filter(q -> !q.isVisited(depth) && isAcceptable(start, q))).orElse(Stream.empty()),
                    Stream.concat(Optional.ofNullable(sorted.get(current)).map(s -> s.stream().filter(q -> !q.isVisited(depth) && isAcceptable(start, q))).orElse(Stream.empty()),
                            Optional.ofNullable(sorted.get(current - 1)).map(s -> s.stream().filter(q -> !q.isVisited(depth) && isAcceptable(start, q))).orElse(Stream.empty()))).toList();
            int index = 0;
            while (index < currentPositions.size()) {
                var position = currentPositions.get(index);
                if (isAcceptable(position, start)) {
                    if (!newRoutes.containsKey(position)) {
                        newRoutes.put(position, new Route());
                    }
                } else {
                    System.out.println("Not acceptable: " + position);
                }
                index++;
            }
            return appendOne(findRoutes(newRoutes, depth), start);
        }

        public List<Position> findRoute() {
            // Potential routes
            Map<Position, Route> routes = new HashMap<>();
            final int current = 1;
            List<Position> currentPositions = sorted.get(current);
            sorted.get(0).forEach(p -> p.setVisited(0));
            int index = 0;
            boolean found = false;

            while(index < currentPositions.size()) {
                var position = currentPositions.get(index);
                if (isAcceptable(position, endPosition)) {
                    if (!routes.containsKey(position)) {
                        routes.put(position, new Route());
                    }
                }
                index++;
            }

            return appendOne(findRoutes(routes, current), endPosition);
        }

        private List<Position> findRoutes(Map<Position, Route> routes, int depth) {
            var results = routes.entrySet().stream().sorted((p, q) -> Integer.compare(q.getKey().distance, p.getKey().distance)).map(r -> {
                r.getKey().setVisited(depth);
                return findRoute(depth+1, r.getKey());
            }).toList();
            return results.stream().filter(s -> !s.isEmpty() && s.get(0).equals(startPosition)).sorted(Comparator.comparingLong(r-> r.size())).findFirst().orElse(List.of());
        }

        private List<Position> traverseFrom(Position position, Route nodes, List<Position> visited) {
            var result = nodes.positions.stream()
                    .filter(q -> isAdjacent(position, q) && ! visited.contains(q) && q.height-position.height>= 0 && q.height-position.height <= 1)
                    .findFirst().map(n -> traverseFrom(n, nodes, appendOne(visited, n))).orElse(List.of());
            return appendOne(result, position);
        }
    }

    private static void execise1(String content) {
        HeightMap map = new HeightMap(content);
        var result =  map.findRoute();
        result.forEach(System.out::println);
        System.out.println("Result = " + result.size());
        // result.forEach(System.out::println);
    }

//    private static void execise2(List<String> lines) {
//    }
}
