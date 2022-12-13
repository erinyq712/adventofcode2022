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
                // execise2(lines);

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

        public Instruction(InstructionType type, int parameter) {
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
            while (! executions.isEmpty() && executions.peek().cycle == cycle) {
                var next = executions.remove();
                next.instruction.execute(this);
            }
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
    };

    private static void execise1(List<String> lines) {
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
        processor.start();
        while (processor.getCycle() < 221 && ! processor.done()) {
            processor.executeNext();
        }
        System.out.println(processor.getSumOfSignalStrengths());
    }

    //private static void execise2(List<String> lines) {

    //}
}
