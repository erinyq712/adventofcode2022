import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Day2 {
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

    private static Round execise1Factory(String line) {
        var values = line.split(" ");
        return new Round(Action.createYours(values[0]), Action.createMine(values[1]));
    }


    private static Round execise2Factory(String line) {
        var values = line.split(" ");
        Action yours = Action.createYours(values[0]);
        Result result = Result.create(values[1]);
        Action mine = yours.getAction(result);
        return new Round(yours, mine);
    }

    private static void execise1(List<String> lines) {
        List<Round> rounds = lines.stream()
                .map(Day2::execise1Factory)
                .collect(Collectors.toList());
        var result = rounds.stream().map(r -> r.getMyScore()).reduce(0, Integer::sum);
        System.out.println("Exercise 1 Score: " + result);
    }


    private static void execise2(List<String> lines) {
        List<Round> rounds = lines.stream()
                .map(Day2::execise2Factory)
                .collect(Collectors.toList());
        var result = rounds.stream().map(r -> r.getMyScore()).reduce(0, Integer::sum);
        System.out.println("Exercise 2 Score: " + result);
    }

    private enum Action {
        Rock(1),
        Paper(2),
        Scissors(3);
        private int mine;
        Action(int score) {
            this.mine = score;
        }

        public static Action createYours(String yours) {
            if (yours.equalsIgnoreCase("A")) {
                return Rock;
            } else if (yours.equalsIgnoreCase("B")) {
                return Paper;
            } else if (yours.equalsIgnoreCase("C")) {
                return Scissors;
            }
            throw new IllegalArgumentException(yours);
        }

        public static Action createMine(String yours) {
            if (yours.equalsIgnoreCase("X")) {
                return Rock;
            } else if (yours.equalsIgnoreCase("Y")) {
                return Paper;
            } else if (yours.equalsIgnoreCase("Z")) {
                return Scissors;
            }
            throw new IllegalArgumentException(yours);
        }

        public int getScore(Action yours) {
            return this.mine + switch(this) {
                case Rock -> switch(yours) {
                    case Rock ->  3;
                    case Paper -> 0;
                    case Scissors -> 6;
                };
                case Paper -> switch(yours) {
                    case Rock -> 6;
                    case Paper -> 3;
                    case Scissors -> 0;
                };
                case Scissors -> switch(yours) {
                    case Rock -> 0;
                    case Paper -> 6;
                    case Scissors -> 3;
                };
            };
        }

        public Action getAction(Result result) {
            return switch(this) {
                case Rock -> switch(result) {
                    case Lose -> Scissors;
                    case Even -> Rock;
                    case Win -> Paper;
                };
                case Paper -> switch(result) {
                    case Lose -> Rock;
                    case Even -> Paper;
                    case Win -> Scissors;
                };
                case Scissors -> switch(result) {
                    case Lose ->  Paper;
                    case Even -> Scissors;
                    case Win -> Rock;
                };
            };
        }
    }

    private enum Result {
        Lose,
        Even,
        Win;

        public static Result create(String value) {
            if (value.equalsIgnoreCase("X")) {
                return Lose;
            } else if (value.equalsIgnoreCase("Y")) {
                return Even;
            } else if (value.equalsIgnoreCase("Z")) {
                return Win;
            }
            throw new IllegalArgumentException(value);
        }
    }

    private record Round(Action yours, Action mine) {
        public int getYourScore() {
            return yours.getScore(mine);
        }
        public int getMyScore() {
            return mine.getScore(yours);
        }
    };

}
