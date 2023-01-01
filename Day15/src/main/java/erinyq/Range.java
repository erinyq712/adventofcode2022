package erinyq;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.max;
import static java.lang.Math.min;

public record Range(Long start, Long endExclusive) {

    public boolean contains(Long p) {
        return p >= start && p < endExclusive;
    }

    public List<Range> split(Long p) {
        if (contains(p)) {
            if (p == start || p == endExclusive-1) {
                return List.of(this);
            } else {
                return List.of(new Range(start, p), new Range(p, endExclusive));
            }
        }
        throw new IllegalArgumentException(String.valueOf(p));
    }

    public int compare(Range range) {
        int sc = Long.compare(start, range.start);
        if (sc == 0) {
            return Long.compare(endExclusive, range.endExclusive);
        } else {
            return sc;
        }
    }

    public boolean isOverlapping(Range current) {
        return start <= current.start && endExclusive >= current.start || current.start <= start && current.endExclusive >= start;
    }

    public boolean isEmpty() {
        return start.equals(endExclusive);
    }

    public Optional<Range> intersection(Range current) {
        if (! isOverlapping(current)) {
            return Optional.empty();
        }
        var isStart =  start <= current.start ? current.start : start;
        var isEndExclusive = endExclusive > current.endExclusive ? current.endExclusive : endExclusive;
        return Optional.of(new Range(isStart, isEndExclusive));
    }

    public List<Range> union(Range current) {
        if (isOverlapping(current)) {
            var uStart =  min(start, current.start);
            var uEndExclusive = max(endExclusive, current.endExclusive);
            return List.of(new Range(uStart, uEndExclusive));
        } else {
            return List.of(this, current);
        }
    }
}
