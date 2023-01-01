package erinyq;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangesTest {

    @Test
    void mergeSingleRange() {
        Ranges r = new Ranges();
        Range r1 = new Range(12L,24L);
        Range r2 = new Range(13L,24L);
        Range r3 = new Range(12L,23L);
        Range r4 = new Range(15L,27L);
        Range r5 = new Range(12L,27L);
        r.addAll(r1,r2,r3,r4,r5);
        var result = r.getRanges();
        assertEquals(1, result.size());
        assertEquals(12L, result.get(0).start());
        assertEquals(27L, result.get(0).endExclusive());
    }

    @Test
    void mergeMultipleRange() {
        Ranges r = new Ranges();
        Range r1 = new Range(12L,24L);
        Range r2 = new Range(13L,24L);
        Range r3 = new Range(12L,23L);
        Range r4 = new Range(25L,27L);
        Range r5 = new Range(32L,37L);
        r.addAll(r1,r2,r3,r4,r5);
        var result = r.getRanges();
        assertEquals(3, result.size());
        assertEquals(12L, result.get(0).start());
        assertEquals(24L, result.get(0).endExclusive());
    }

    @Test
    void mergeDifference() {
        Ranges r = new Ranges();
        Range r1 = new Range(12L,24L);
        Range r2 = new Range(13L,24L);
        Range r3 = new Range(12L,23L);
        Range r4 = new Range(25L,27L);
        Range r5 = new Range(32L,37L);
        r.addAll(r1,r2,r3,r4,r5);
        var result = r.difference().getRanges();
        assertEquals(2, result.size());
        assertEquals(24L, result.get(0).start());
        assertEquals(25L, result.get(0).endExclusive());
    }

    @Test
    void intersection() {
        Ranges r = new Ranges();
        Range r1 = new Range(12L,24L);
        Range r2 = new Range(13L,24L);
        Range r3 = new Range(12L,23L);
        Range r4 = new Range(25L,27L);
        Range r5 = new Range(32L,37L);
        r.addAll(r1,r2,r3,r4,r5);
        var result = r.intersection(new Range(13L,15L)).getRanges();
        assertEquals(1, result.size());
        assertEquals(13L, result.get(0).start());
        assertEquals(15L, result.get(0).endExclusive());
    }
}