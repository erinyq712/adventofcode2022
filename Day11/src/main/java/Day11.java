import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class Day11 {
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

    public record MonkeyData(int monkeyId,
                             Deque<BigInteger> items,
                             BiFunction<BigInteger, MonkeyData, BigInteger> operand,
                             BigInteger value,
                             BigInteger dividable,
                             int ifTrue,
                             int ifFalse) {
    }

    static class Monkey {
        public static final BigInteger BIG_INTEGER_2 = BigInteger.valueOf(2L);
        private static long counter = 0L;
        private static List<BigInteger> primeNumbers = new ArrayList<>();

        static {
            primeNumbers = getPrimes(BigInteger.valueOf(2000L));
        }

        private static List<BigInteger> getPrimes(BigInteger limit) {
            if (primeNumbers.isEmpty()) {
                primeNumbers.add(BIG_INTEGER_2);
                primeNumbers.add(BigInteger.valueOf(3L));
                primeNumbers.add(BigInteger.valueOf(5L));
                primeNumbers.add(BigInteger.valueOf(7L));
                primeNumbers.add(BigInteger.valueOf(11L));
                primeNumbers.add(BigInteger.valueOf(13L));
                primeNumbers.add(BigInteger.valueOf(17L));
                primeNumbers.add(BigInteger.valueOf(19L));
                primeNumbers.add(BigInteger.valueOf(23L));
                primeNumbers.add(BigInteger.valueOf(31L));
            }
            BigInteger max = primeNumbers.get(primeNumbers.size()-1);
            if (max.compareTo(limit) < 0) {
                BigInteger current = max.add(BIG_INTEGER_2);
                while (current.compareTo(limit) < 0) {
                    final BigInteger value = current;
                    final BigInteger maxCheck = current.sqrt().add(BigInteger.ONE);
                    if (primeNumbers.stream().filter(p -> p.compareTo(maxCheck) < 0)
                            .noneMatch(p -> checkRelativelyPrime(p,value))) {
                        primeNumbers.add(current);
                    }
                    current = current.add(BIG_INTEGER_2);
                }
            }
            return primeNumbers;
        }

        private static boolean checkRelativelyPrime(BigInteger prime, BigInteger current) {
            return current.divideAndRemainder(prime)[1].compareTo(BigInteger.ZERO) == 0;
        }

        private final List<Monkey> monkeys;
        private final MonkeyData data;
        private BigInteger total;

        public Monkey(MonkeyData data, List<Monkey> monkeys) {
            this.data = data;
            this.monkeys = monkeys;
            total=BigInteger.ZERO;
        }

        public long id() {
            return data.monkeyId;
        }

        public void add(BigInteger item) {
            data.items.add(item);
        }

        public BigInteger pop() {
            return data.items.pop();
        }

        public void handle1() {
            while(! data.items.isEmpty()) {
                counter++;
                total = total.add(BigInteger.ONE);
                var worryLevel = pop();
                BigInteger worryLevelProduct = data.operand.apply(worryLevel, data);
                BigInteger reliefValue = worryLevelProduct.divide(BigInteger.valueOf(3L));
                BigInteger[] dv = reliefValue.divideAndRemainder(data.dividable);
                if (dv[1].compareTo(BigInteger.ZERO) == 0) {
                    monkeys.get(data.ifTrue).add(reliefValue);
                    // System.out.println(counter + " " + id() + " sending to: " + data.ifTrue);
                } else {
                    monkeys.get(data.ifFalse).add(reliefValue);
                    // System.out.println(counter + " " + id() + " sending to: " + data.ifFalse);
                }
            }
        }

        public void handle2(BigInteger product) {
            while(! data.items.isEmpty()) {
                counter++;
                total = total.add(BigInteger.ONE);
                var worryLevel = pop();
                // Remainder over product of divisiors is equivalent
                // to full value when doing modulo arithmetic
                BigInteger[] dv0 = worryLevel.divideAndRemainder(product);
                BigInteger reliefValue = data.operand.apply(dv0[1], data);
                BigInteger[] dv = reliefValue.divideAndRemainder(data.dividable);
                if (dv[1].compareTo(BigInteger.ZERO) == 0 || dv[0].compareTo(BigInteger.ZERO) == 0 && dv[1].compareTo(data.dividable) == 0) {
                    monkeys.get(data.ifTrue).add(reliefValue);
                    // System.out.println(counter + " " + id() + " sending to: " + data.ifTrue);
                } else {
                    monkeys.get(data.ifFalse).add(reliefValue);
                    // System.out.println(counter + " " + id() + " sending to: " + data.ifFalse);
                }
            }
        }

        @Override
        public String toString() {
            return "Monkey{" +
                    ", data=" + data +
                    ", total=" + total +
                    '}';
        }
    }

    private static final String TEMPLATE = """
Monkey (\\d+):
  Starting items: (\\d+(, \\d+)*)
  Operation: new = old ([*+]) (\\d+|old)
  Test: divisible by (\\d+)
    If true: throw to monkey (\\d+)
    If false: throw to monkey (\\d+)""";

    private static final BiFunction<BigInteger, MonkeyData, BigInteger> plus = (x,y) -> y == null ? x.add(x) : x.add(y.value);

    private static final BiFunction<BigInteger, MonkeyData, BigInteger> multiply = (x,y) -> y.value == null ? (x.multiply(x)) : x.multiply(y.value);
    private static final BiFunction<BigInteger, MonkeyData, BigInteger> noop = (x,y) -> x;

    private static final Pattern monkeyPattern = Pattern.compile(TEMPLATE);

    private static void execise1(String content) {
        ArrayList<Monkey> monkeys = getMonkeys(content, multiply);
        for(int i = 0; i < 20; i++) {
            monkeys.forEach(Monkey::handle1);
        }
        printResult(monkeys);
    }

    private static ArrayList<Monkey> getMonkeys(String content, BiFunction<BigInteger, MonkeyData, BigInteger> multiply) {
        var monkeys = new ArrayList<Monkey>();
        var matcher = monkeyPattern.matcher(content);
        while(matcher.find()) {
            var monkeyId = Integer.parseInt(matcher.group(1));
            ArrayDeque<BigInteger> items = Arrays.stream(matcher.group(2).split(",\\s*")).map(Long::parseLong).map(BigInteger::valueOf).collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll);
            var operand = switch(matcher.group(4)) {
                case "+" -> plus;
                case "*" -> multiply;
                default -> noop;
            };
            var value = switch(matcher.group(5)) {
                case "old" -> null;
                default -> Integer.parseInt(matcher.group(5));
            };
            var dividable = Long.parseLong(matcher.group(6));
            var ifTrue = Integer.parseInt(matcher.group(7));
            var ifFalse = Integer.parseInt(matcher.group(8));
            monkeys.add(new Monkey(new MonkeyData(monkeyId, items, operand, Optional.ofNullable(value).map(BigInteger::valueOf).orElse(null), BigInteger.valueOf(dividable), ifTrue, ifFalse), monkeys));
        }
        return monkeys;
    }

    private static void execise2(String content) {
        ArrayList<Monkey> monkeys = getMonkeys(content, multiply);
        BigInteger product = monkeys.stream().map(m->m.data.dividable).reduce(BigInteger::multiply).orElse(BigInteger.ONE);
        // monkeys.stream().forEach(System.out::println);
        for(int i = 0; i < 10000; i++) {
            monkeys.forEach(monkey -> monkey.handle2(product));
        }
        printResult(monkeys);
    }

    private static void printResult(ArrayList<Monkey> monkeys) {
        var result = monkeys.stream().sorted((m,n) -> n.total.compareTo(m.total)).toList();
        // result.forEach(m -> System.out.println(m.id() + ": " + m.total));
        BigInteger monkeyBusiness = result.get(0).total.multiply(result.get(1).total);
        System.out.println("Monkey business: " + monkeyBusiness);
    }
}
