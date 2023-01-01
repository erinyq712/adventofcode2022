package erinyq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class Ranges {

    private List<Range> ranges;

    public Ranges() {
        ranges = new ArrayList<>();
    }

    public Ranges(Range p) {
        this();
        append(p);
    }

    public Ranges(List<Range> ranges) {
        this();
        addAll(ranges);
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public void append(Range r) {
        ranges.add(r);
    }

    public void merge() {
        ranges = ranges.stream().sorted(Range::compare).toList();
        boolean isOverlapping = true;
        while (isOverlapping) {
            final List<Range> result = new ArrayList<>();
            if (! ranges.isEmpty()) {
                Range current = ranges.get(0);
                for (int i = 1; i < ranges.size(); i++) {
                    var next = ranges.get(i);
                    var union = current.union(next);
                    if (union.size() > 1) {
                        result.add(union.get(0));
                    }
                    current = union.get(union.size() - 1);
                }
                result.add(current);
            }
            isOverlapping = result.stream().anyMatch(r -> result.stream().anyMatch(p -> ! p.equals(r) && p.isOverlapping(r)));
            ranges = result;
        }
    }

    public void addAll(Range ... r) {
        for(Range r1 : r) {
            append(r1);
        }
        merge();
    }
    public void addAll(List<Range> ranges) {
        this.ranges.addAll(ranges);
        merge();
    }

    public Ranges difference() {
        var union = ranges;
        if (ranges.size() == 1) {
            return new Ranges();
        } else {
            var diff = new Ranges();
            int current = 0;
            while (current < union.size()-1) {
                var first = union.get(current);
                var second = union.get(current+1);
                diff.append(new Range(first.endExclusive(), second.start()));
                current++;
            }
            return diff;
        }
    }

    public Ranges intersection(Range range) {
        return new Ranges(ranges.stream().map( r -> r.intersection(range).orElse(null)).filter(Objects::nonNull).filter(r -> ! r.isEmpty()).toList());
    }
}
