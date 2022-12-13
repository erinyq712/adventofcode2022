import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.IntStream;

public class Day10 {
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

    enum InstructionType {
        NOOP(1), ADDX(2);

        private int cycles;

        InstructionType(int cycles) {
            this.cycles = cycles;
        }

    }
    abstract static class Instruction {
        protected final InstructionType type;
        protected final int parameter;

        protected Instruction(InstructionType type, int parameter) {
            this.type = type;
            this.parameter = parameter;
        }

        public int cycles() {
            return type.cycles;
        }

        public abstract void execute(Processor p);
    }

    static class NoopInstruction extends Instruction {
        public NoopInstruction(int parameter) {
            super(InstructionType.NOOP, parameter);
        }
        public void execute(Processor p) {
            // NOOP
        }
    }

    static class AddxInstruction extends Instruction {
        public AddxInstruction(int parameter) {
            super(InstructionType.ADDX, parameter);
        }
        public void execute(Processor p) {
            p.addx(parameter);
        }
    }

    record Execution(Instruction instruction, long cycle) {
    }

    static class Display {
        public static final int SIZE = 40;
        public static final int ROWS = 6;

        private char[][] buffers = new char[ROWS][SIZE];

        public Display() {
            IntStream.range(0,ROWS).forEach(i -> buffers[i] = getDisplayBuffer());
        }
        private int row = 0;
        private int position = 0;

        private char[] getDisplayBuffer() {
            return ".".repeat(SIZE).toCharArray();
        }

        public List<String> get() {
            row = 0;
            position = 0;
            return IntStream.range(0,ROWS).mapToObj(i -> String.valueOf(buffers[i])).toList();
        }

        public void set(int x) {
            if (x-1 == position || x == position || x+1 == position) {
                buffers[row][position] = '#';
            }
            if (position == Display.SIZE-1) {
                position = 0;
                row++;
            } else {
                position++;
            }
        }

        public boolean ready() {
            return row == ROWS;
        }
    }

    static class Processor {
        private long cycle;
        private int x;
        private final Queue<Execution> executions;

        private final List<Long> signalStrengths;

        public Processor() {
            this.cycle = 0;
            this.executions = new ArrayDeque<>();
            this.x = 1;
            this.signalStrengths = new ArrayList<>();
        }

        public List<Long> getSignalStrengths() {
            return signalStrengths;
        }

        public void push(Instruction instruction) {
            cycle += instruction.type.cycles;
            executions.add(new Execution(instruction,cycle));
        }

        public void executeNext() {
            cycle++;
            if (cycle == 20 || (cycle > 20 && (cycle-20) % 40 == 0)) {
                signalStrengths.add(cycle*x);
            }
            execute();
        }

        public void addx(int inc) {
            x += inc;
        }

        public long getCycle() {
            return cycle;
        }

        public boolean done() {
            return executions.isEmpty();
        }

        public void start() {
            cycle = 0;
        }

        public long getSumOfSignalStrengths() {
            return signalStrengths.stream().reduce(Long::sum).orElse(0L);
        }

        public void display(Display display) {
            // Must set last pixel before setting value in next row
            display.set(x);
            executeNext();
        }

        private void execute() {
            while (! executions.isEmpty() && executions.peek().cycle == cycle) {
                var next = executions.remove();
                next.instruction.execute(this);
            }
        }
    }

    private static void execise1(List<String> lines) {
        Processor processor = getProcessor(lines);
        processor.start();
        while (processor.getCycle() < 221 && ! processor.done()) {
            processor.executeNext();
        }
        System.out.println(processor.getSumOfSignalStrengths());
    }

    private static Processor getProcessor(List<String> lines) {
        Processor processor = new Processor();
        lines.stream().forEach(l -> {
            String[] words = l.split(" ");
            var instructionType = InstructionType.valueOf(words[0].toUpperCase());
            if (instructionType == InstructionType.NOOP) {
                processor.push(new NoopInstruction(0));
            } else if (instructionType == InstructionType.ADDX) {
                processor.push(new AddxInstruction(words.length>1 ? Integer.parseInt(words[1]) : 0));
            } else {
                throw new IllegalArgumentException("Not implemented");
            }
        });
        return processor;
    }

    private static void execise2(List<String> lines) {
        Processor processor = getProcessor(lines);
        processor.start();
        var display = new Display();
        while (! display.ready()) {
            processor.display(display);
        }
        display.get().forEach(System.out::println);
    }
}
