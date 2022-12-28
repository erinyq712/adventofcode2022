import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day13 {
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

    private static class Token {
    }

    public static class GroupStart extends Token {
        @Override
        public String toString() {
            return "[";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GroupStart;
        }
    }

    public static class GroupEnd extends Token {
        public String toString() {
            return "]";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GroupEnd;
        }
    }

    public static class Comma extends Token {
        public String toString() {
            return "";
        }
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Comma;
        }
    }

    public static class NumberToken extends Token {
        private Integer number;
        public NumberToken(String s) {
            number = Integer.parseInt(s);
        }
        public String toString() {
            return String.valueOf(number);
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NumberToken nt) {
                return number == nt.number;
            } else {
                return false;
            }
        }
    }

    private static class Lexer {
        private String line;
        private int start = 0;

        private Token token;

        public Lexer(String line) {
            this.line = line;
        }

        public boolean isMatch() {
            try {
                nextToken();
                return true;
            } catch(Exception e) {
                return false;
            }
        }

        public Token next() {
            if (token == null) {
                nextToken();
            }
            var nextToken = token;
            token = null;
            return nextToken;
        }

        private void nextToken() {
            if (start >= line.length()) {
                throw new RuntimeException("End");
            }
            if (line.charAt(start) == '[') {
                token = new GroupStart();
            } else if (line.charAt(start) == ']') {
                token = new GroupEnd();
            } else if (line.charAt(start) == ',') {
                token = new Comma();
            } else {
                int current = start+1;
                try {
                    while (Integer.parseInt(line.substring(current, current + 1)) >= 0) {
                        current++;
                    }
                } catch(NumberFormatException e) {
                    // DO NOTHING
                }
                token = new NumberToken(line.substring(start, current));
            }
            start++;
        }
    }

    record Expression(List<Token> tokens) {
        @Override
        public boolean equals(Object o) {
            if (o instanceof Expression that) {
                return Objects.equals(tokens, that.tokens);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(tokens);
        }
    }

    public static int compareNodes(Node n1, Node n2) {
        if (n1.isLeaf() && n2.isLeaf()) {
            var c = Integer.compare(n1.number, n2.number);
            if (c < 0) {
                return -1;
            } else if (c > 0) {
                return 1;
            } else {
                return 0;
            }
        } else if (n1.isLeaf()) {
            var newNode = new Node();
            newNode.add(n1);
            return compareNodes(newNode, n2);
        } else if (n2.isLeaf()) {
            var newNode = new Node();
            newNode.add(n2);
            return compareNodes(n1, newNode);
        } else {
            var it1 = n1.children.iterator();
            var it2 = n2.children.iterator();
            while (it1.hasNext() && it2.hasNext()) {
                var suv = compareNodes(it1.next(), it2.next());
                if (suv != 0) {
                    return suv;
                }
            }
            if (! it1.hasNext() && ! it2.hasNext()) {
                return 0;
            } else {
                return it1.hasNext() ? 1 : -1;
            }
        }
    }

    record Node(int number, List<Node> children) {
        public Node() {
            this(-1, new ArrayList<>());
        }

        public Node(Integer number) {
            this(number, null);
        }

        public void add(Node next) {
            if (isLeaf()) {
                throw new UnsupportedOperationException("Cannot add to node");
            }
            children.add(next);
        }

        public boolean isLeaf() {
            return children == null;
        }
    }

    static class NodeTree {

        private Node root;

        public NodeTree(Expression e) {
            this(e.tokens);

        }
        public NodeTree(List<Token> tokens) {
            var it = tokens.iterator();
            if (it.hasNext() && it.next() instanceof GroupStart) {
                root = new Node();
                buildTree(root, it);
            }
        }

        private void buildTree(Node root, Iterator<Token> it) {
            while (it.hasNext()) {
                var nextToken = it.next();
                if (nextToken instanceof GroupStart ts) {
                    var next = new Node();
                    root.add(next);
                    buildTree(next, it);
                } else if (nextToken instanceof GroupEnd ts) {
                    return;
                } else if (nextToken instanceof NumberToken nt) {
                    var next = new Node(nt.number);
                    root.add(next);
                } else if (nextToken instanceof Comma nt) {
                    // Do nothing
                }
            }
        }

        public Node getRoot() {
            return root;
        }
    }

    private static int comparePackets(List<Token> a, List<Token> b) {
        NodeTree ng1 = new NodeTree(a);
        NodeTree ng2 = new NodeTree(b);
        return compareNodes(ng1.getRoot(), ng2.getRoot());
    }

    private static int comparePackets(Expression a, Expression b) {
        NodeTree ng1 = new NodeTree(a);
        NodeTree ng2 = new NodeTree(b);
        return compareNodes(ng1.getRoot(), ng2.getRoot());
    }

    private static final Day13.GroupStart GROUP_START = new Day13.GroupStart();
    private static final Day13.GroupEnd GROUP_END = new Day13.GroupEnd();
    public static final Day13.NumberToken NUMBER_TOKEN_2 = new Day13.NumberToken("2");
    public static final Day13.NumberToken NUMBER_TOKEN_6 = new Day13.NumberToken("6");

    private static final Expression DELIMITER2 = new Expression(List.of(GROUP_START, GROUP_START, NUMBER_TOKEN_2, GROUP_END, GROUP_END));
    private static final Expression DELIMITER6 = new Expression(List.of(GROUP_START, GROUP_START, NUMBER_TOKEN_6, GROUP_END, GROUP_END));

    private static void execise1(List<String> lines) {
        var expression = lines.stream().map(l -> {
            var lexer = new Lexer(l);
            var tokens = new ArrayList<Token>();
            while(lexer.isMatch()) {
                tokens.add(lexer.next());
            }
            return tokens;
        }).toList();

        var result = IntStream.range(0, (expression.size()+1)/3).map(i -> {
            var exp1 = expression.get(i*3);
            var exp2 = expression.get(i*3 +1);
            NodeTree ng1 = new NodeTree(exp1);
            NodeTree ng2 = new NodeTree(exp2);
            var nodeCheck = compareNodes(ng1.getRoot(), ng2.getRoot());
            if (nodeCheck != -1) {
                return nodeCheck;
            }
            return -1;
        }).toArray();

        var sum = IntStream.range(0, (expression.size()+1)/3).map(i -> {
            var exp1 = expression.get(i*3);
            var exp2 = expression.get(i*3 +1);
            System.out.println((i+1) + " " + result[i]);
            return result[i] == -1 ? i+1 : 0;
        }).sum();
        System.out.println("Sum: " + sum);
    }

    private static void execise2(List<String> lines) {
        var expressions = lines.stream().map(l -> {
            var lexer = new Lexer(l);
            var tokens = new ArrayList<Token>();
            while (lexer.isMatch()) {
                tokens.add(lexer.next());
            }
            return tokens;
        }).filter(t -> !t.isEmpty()).map(l -> new Expression(l)).toList();


        var result = Stream.concat(expressions.stream(), Stream.of(DELIMITER2, DELIMITER6)).sorted(Day13::comparePackets).toList();

        var dividers = IntStream.range(0, expressions.size())
                .filter(i -> result.get(i).equals(DELIMITER2) || result.get(i).equals(DELIMITER6))
                .map(i->i+1)
                .toArray();

        System.out.println("Result: " + (dividers[0] * dividers[1]));
    }

}

