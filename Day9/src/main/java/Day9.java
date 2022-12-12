import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Integer.signum;
import static java.lang.Math.abs;
import static java.lang.Math.max;

public class Day9 {
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

    enum Direction {
        R,L,U,D
    }

    record Move(Direction direction, int steps) {
    }

    record Position(int x, int y) {
        public Position moveRight(int steps) {
            return new Position(x+steps, y);
        }

        public Position moveLeft(int steps) {
            return new Position(x-steps, y);
        }

        public Position moveUp(int steps) {
            return new Position(x, y+steps);
        }

        public Position moveDown(int steps) {
            return new Position(x, y-steps);
        }

        public int distance(Position pos) {
            return max(abs(x-pos.x),abs(y-pos.y));
        }

        public Position delta(Position pos) {
            return new Position(signum(x - pos.x), signum(y - pos.y));
        }
    }

    record RopePosition(Position head, Position tail, Set<Position> tailPositions) {
    }

    record RopePosition2(List<Position> knots, Set<Position> tailPositions) {
    }

    private static void execise1(List<String> lines) {
        var moves = lines.stream().map(line -> {
            var values = line.split(" ");
            return new Move(Direction.valueOf(values[0]), Integer.valueOf(values[1]));
        }).toList();
        Position startPosition = new Position(0, 0);
        Set<Position> tailPositions = new HashSet<>();
        tailPositions.add(startPosition);
        RopePosition currentPosition = new RopePosition(startPosition, startPosition, tailPositions);
        var iterator = moves.iterator();
        while(iterator.hasNext()) {
            var move = iterator.next();
            currentPosition = next(currentPosition, move);
            // System.out.println(currentPosition + ": " + currentPosition.head.distance(currentPosition.tail));
        }

        System.out.println("Tail positions:" + currentPosition.tailPositions.size());
    }

    private static RopePosition next(RopePosition start, Move move) {
        var currentHead = start.head;
        var currentTail = start.tail;
        Set<Position> tailPositions = new HashSet<>();
        tailPositions.add(currentTail);
        int counter = 0;
        while (counter < move.steps) {
            counter++;
            Position headPosition = switch (move.direction) {
                case R -> currentHead.moveRight(1);
                case L -> currentHead.moveLeft(1);
                case U -> currentHead.moveUp(1);
                case D -> currentHead.moveDown(1);
            };
            int xmove = currentTail.x;
            int ymove = currentTail.y;
            var delta = headPosition.delta(currentHead);
            var distance = currentTail.distance(headPosition);
            if (distance > 1) {
                if (headPosition.y == currentHead.y) {
                    xmove = headPosition.x - delta.x;
                    if (currentTail.y != headPosition.y) {
                        ymove = headPosition.y;
                    }
                } else if (headPosition.x == currentHead.x) {
                    ymove = headPosition.y - delta.y;
                    if (currentTail.x != headPosition.x) {
                        xmove = headPosition.x;
                    }
                }
            } else {
                ymove = currentTail.y;
                xmove = currentTail.x;
            }
            currentTail = new Position(xmove, ymove);
            tailPositions.add(currentTail);
            currentHead = headPosition;
        }
        tailPositions.addAll(start.tailPositions);
        return new RopePosition(currentHead, currentTail, tailPositions);
    }

    private static void execise2(List<String> lines) {
        var moves = lines.stream().map(line -> {
            var values = line.split(" ");
            return new Move(Direction.valueOf(values[0]), Integer.valueOf(values[1]));
        }).toList();
        Position startPosition = new Position(0, 0);
        Position[] knots = new Position[] { startPosition, startPosition, startPosition, startPosition, startPosition, startPosition, startPosition, startPosition, startPosition, startPosition };
        Set<Position> tailPositions = new HashSet<>();
        tailPositions.add(startPosition);
        RopePosition2 currentPosition = new RopePosition2(Arrays.asList(knots), tailPositions);
        var iterator = moves.iterator();
        while(iterator.hasNext()) {
            var move = iterator.next();
            currentPosition = next2(currentPosition, move);
        }

        System.out.println("Tail positions:" + currentPosition.tailPositions.size());
    }

    private static RopePosition2 next2(RopePosition2 start, Move move) {
        if (start.knots.size() < 2) {
            throw new IllegalArgumentException("Rope must have at least two knots");
        }
        var currentHead = start.knots.get(0);
        var currentTail = start.knots.subList(1, start.knots.size());
        return processHead(currentHead, currentTail, start.tailPositions, move);
    }

    // Move head 1 step
    // Adjust tail positions
    // Repeat until the move is complete
    private static RopePosition2 processHead(Position currentHead, List<Position> tail, Set<Position> tailPositions, Move move) {
        if (move.steps > 0) {
            Position newHeadPosition = switch (move.direction) {
                case R -> currentHead.moveRight(1);
                case L -> currentHead.moveLeft(1);
                case U -> currentHead.moveUp(1);
                case D -> currentHead.moveDown(1);
            };
            // Move tail
            var currentTail = getTailPositions(newHeadPosition, currentHead, tail, tailPositions);
            return processHead(newHeadPosition, currentTail, tailPositions, new Move(move.direction, move.steps - 1));
        } else {
            var result = new ArrayList<Position>();
            result.add(currentHead);
            result.addAll(tail);
            return new RopePosition2(result, tailPositions);
        }
    }

    private static List<Position> getTailPositions(Position newHeadPosition, Position currentHead, List<Position> tail, Set<Position> tailPositions) {
        if (tail.size() == 0) {
            tailPositions.add(newHeadPosition);
            return List.of();
        }
        var nextTail = getTailPosition(newHeadPosition, currentHead, tail.get(0));
        if (nextTail == tail.get(0)) {
            return tail;
        } else {
            var result = new ArrayList<Position>();
            result.add(nextTail);
            // Could terminate recursion here
            var nextTailTail = getTailPositions(nextTail, tail.get(0), tail.size() > 1 ? tail.subList(1, tail.size()) : List.of(), tailPositions);
            result.addAll(nextTailTail);
            return result;
        }
    }

    // Return new position
    private static Position getTailPosition(Position headPosition, Position currentHead, Position currentTail) {
        int xmove = currentTail.x;
        int ymove = currentTail.y;
        var delta = headPosition.delta(currentTail);
        var distance = currentTail.distance(headPosition);
        if (distance > 1) {
            if (currentTail.y != headPosition.y && currentTail.x != headPosition.x) {
                // Moves based on head
                // ymove = headPosition.y - delta.y;
                // xmove = headPosition.x - delta.x;
                // Moves based on tail
                ymove = currentTail.y + delta.y;
                xmove = currentTail.x + delta.x;
            } else if (headPosition.y == currentTail.y) {
                xmove = headPosition.x - delta.x;
            } else if (headPosition.x == currentTail.x) {
                ymove = headPosition.y - delta.y;
            }
            return new Position(xmove, ymove);
        } else {
            return currentTail;
        }
    }
}

