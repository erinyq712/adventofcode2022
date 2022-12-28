import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Day13Test {

    private static final Day13.GroupStart GROUP_START = new Day13.GroupStart();
    private static final Day13.GroupEnd GROUP_END = new Day13.GroupEnd();
    public static final Day13.NumberToken NUMBER_TOKEN_4 = new Day13.NumberToken("4");
    public static final Day13.NumberToken NUMBER_TOKEN_5 = new Day13.NumberToken("5");

    @Test
    void exercise1TestInput() {
        Day13.NodeTree ng1 = new Day13.NodeTree(List.of(GROUP_START, GROUP_START, NUMBER_TOKEN_4, GROUP_END, NUMBER_TOKEN_5, GROUP_END));
        Day13.NodeTree ng2 = new Day13.NodeTree(List.of(GROUP_START, NUMBER_TOKEN_4, GROUP_START, NUMBER_TOKEN_5, GROUP_END, GROUP_END));
        var nodeCheck = Day13.compareNodes(ng1.getRoot(), ng2.getRoot());
        assertEquals(1, nodeCheck);
    }
}