import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

public class Day8 {
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
    }

    private static class Map {
        private List<ArrayList<Integer>> heights;
        private Set<Position> hiddenInRows;
        private Set<Position> hiddenInColums;

        public Map(List<ArrayList<Integer>> heights) {
            this.heights = heights;
            hiddenInRows = getHiddenInRows().stream().flatMap(l -> l.stream()).collect(Collectors.toSet());
            hiddenInColums = getHiddenInColumns().stream().flatMap(l -> l.stream()).collect(Collectors.toSet());
        }

        public int at(int rowPos, int pos) {
            if (rowPos < 0 || rowPos >= heights.size()) {
                return -1;
            }
            var row = heights.get(rowPos);
            if (pos < 0 || pos >= row.size()) {
                return -1;
            }
            return row.get(pos);
        }

        public int at(Position p) {
            return at(p.x, p.y);
        }


        private Set<Position> getHiddenInRow(int maxHeight, int row, int pos, UnaryOperator<Integer> next) {
            var nextPos = next.apply(pos);
            var height = at(row,pos);
            var nextHeight = at(row,nextPos);
            if (height <= maxHeight) {
                var positions = new HashSet<Position>();
                positions.add(new Position(row,pos));
                if (nextHeight != -1) {
                    positions.addAll(getHiddenInRow(maxHeight, row, nextPos, next));
                }
                return positions;
            } else {
                return getHiddenInRow(height, row, nextPos, next);
            }
        }

        private Set<Position> getHiddenInColumn(int maxHeight, int row, int pos, UnaryOperator<Integer> next) {
            var nextRow = next.apply(row);
            var height = at(row,pos);
            var nextHeight = at(nextRow,pos);
            if (height <= maxHeight) {
                var positions = new HashSet<Position>();
                positions.add(new Position(row,pos));
                if (nextHeight != -1) {
                    positions.addAll(getHiddenInColumn(maxHeight, nextRow, pos, next));
                }
                return positions;
            } else {
                return getHiddenInColumn(height, nextRow, pos, next);
            }
        }

        public Set<Position> getHiddenInRowForward(int row) {
            return getHiddenInRow(-1, row, 0, p -> p+1);
        }

        public Set<Position> getHiddenInRowBackwards(int row) {
            var firstIndex = heights.get(row).size()-1;
            return getHiddenInRow(-1, row, firstIndex, p -> p-1);
        }

        public Set<Position> getHiddenInColumnDownwards(int column) {
            return getHiddenInColumn(-1, 0, column, p -> p+1);
        }

        public Set<Position> getHiddenInRowUpwards(int column) {
            var firstRow = heights.size()-1;
            return getHiddenInColumn(-1, firstRow, column, p -> p-1);
        }

        public List<Set<Position>> getHiddenInRows() {
            return IntStream.range(0, heights.size()).mapToObj(i -> {
                var forwardPositions =  getHiddenInRowForward(i);
                var backwardPositions = getHiddenInRowBackwards(i);
                return intersect(forwardPositions, backwardPositions);
            }).toList();
        }

        public List<Set<Position>> getHiddenInColumns() {
            var width = heights.size() > 0 ? heights.get(0).size() : 0;
            return IntStream.range(0, width).mapToObj(i -> {
                var forwardPositions =  getHiddenInColumnDownwards(i);
                var backwardPositions = getHiddenInRowUpwards(i);
                return intersect(forwardPositions, backwardPositions);
            }).toList();
        }

        private boolean isHidden(Position p) {
            return hiddenInRows.contains(p) && hiddenInColums.contains(p);
        }

        private Position getHiddenPosition(int row, int column){
            var pos = new Position(row, column);
            return isHidden(pos) ? pos: null;
        }

        private Set<Position> getHiddenPositions(int row) {
            return IntStream.range(0, heights.get(row).size())
                    .mapToObj(c -> getHiddenPosition(row,c))
                    .filter(Objects::nonNull).collect(Collectors.toSet());
        }

        public Set<Position> getHidden() {
            return IntStream.range(0, heights.size()).mapToObj(this::getHiddenPositions).flatMap(Set::stream).collect(Collectors.toSet());
        }

        public long size() {
            return heights.size() * (heights.size()> 0 ? heights.get(0).size() : 0);
        }

        public long getScore(Position p) {
            return getRightScore(p)*getLeftScore(p)*getUpScore(p)*getDownScore(p);
        }

        private long getRightScore(Position p) {
            return getHorizontalScore(p,y->y+1);
        }
        private long getLeftScore(Position p) {
            return getHorizontalScore(p,y->y-1);
        }

        private long getHorizontalScore(Position p, UnaryOperator<Integer> next) {
            int pos = next.apply(p.y);
            var height = at(p);
            var nextHeight = at(p.x, pos);
            while (nextHeight < height && nextHeight > -1) {
                pos = next.apply(pos);
                nextHeight = at(p.x, pos);
            }
            return abs(nextHeight == -1 ? pos-next.apply(p.y) : pos-p.y);
        }

        private long getUpScore(Position p) {
            return getVerticalScore(p,x->x-1);
        }
        private long getDownScore(Position p) {
            return getVerticalScore(p,x->x+1);
        }

        private long getVerticalScore(Position p, UnaryOperator<Integer> next) {
            int row = next.apply(p.x);
            var height = at(p);
            var nextHeight = at(row, p.y);
            while (nextHeight < height && nextHeight > -1) {
                row = next.apply(row);
                nextHeight = at(row, p.y);
            }
            return abs(nextHeight == -1 ? row-next.apply(p.x) : row-p.x);
        }

        record Score (Position position, long score) {
        }

        public Score getBestPosition() {
            var positions = IntStream.range(0, heights.size()).mapToObj(x -> {
                var columns = heights.get(x);
                return IntStream.range(0, columns.size()).mapToObj(y -> new Position(x,y)).toList();
            }).flatMap(Collection::stream).map(p -> new Score(p, getScore(p))).sorted((s1, s2) -> Long.compare(s2.score, s1.score)).toList();
            return positions.get(0);
        }
    }

    private static Set<Position> intersect(Set<Position> forwardPositions, Set<Position> backwardPositions) {
        Set<Position> result = new HashSet<>(forwardPositions);
        result.retainAll(backwardPositions);
        return result;
    }

    private static void execise1(List<String> lines) {
        var map = getMap(lines);
        var hidden = map.getHidden();
        hidden.forEach(System.out::println);
        System.out.println(map.size()-hidden.size());

    }

    private static Map getMap(List<String> lines) {
        return new Map(lines.stream().map(line -> {
            int width = line.length();
            return IntStream.range(0, width).map(i -> Integer.parseInt(line.substring(i, i + 1))).collect(ArrayList<Integer>::new, ArrayList<Integer>::add, ArrayList<Integer>::addAll);
        }).toList());
    }

    private static void execise2(List<String> lines) {
        var map = getMap(lines);
        var bestPos = map.getBestPosition();
        System.out.println(bestPos);
    }
}
