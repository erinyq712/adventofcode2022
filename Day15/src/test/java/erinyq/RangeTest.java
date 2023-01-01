package erinyq;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangeTest {

    @Test
    void contains() {
        Range r = new Range(12L,24L);
        assertTrue(r.contains(12L));
        assertTrue(r.contains(13L));
        assertFalse(r.contains(24L));
    }

    @Test
    void split() {
        Range r = new Range(12L,24L);
        var result = r.split(13L);
        assertTrue(result.size()==2);
        assertEquals(12L, result.get(0).start());
        assertEquals(13L, result.get(0).endExclusive());
        assertEquals(13L, result.get(1).start());
        assertEquals(24L, result.get(1).endExclusive());
    }

    @Test
    void splitFail() {
        Range r = new Range(12L,24L);
        assertThrows(IllegalArgumentException.class, () -> r.split(33L));
    }

    @Test
    void compare() {
        Range r1 = new Range(12L,24L);
        Range r2 = new Range(13L,24L);
        Range r3 = new Range(12L,23L);
        assertEquals(0, r1.compare(r1));
        assertEquals(-1, r1.compare(r2));
        assertEquals(1, r2.compare(r1));
        assertEquals(1, r1.compare(r3));
        assertEquals(-1, r3.compare(r1));
    }

    @Test
    void isOverlapping() {
        Range r1 = new Range(12L,24L);
        Range r2 = new Range(13L,24L);
        Range r3 = new Range(12L,23L);
        Range r4 = new Range(24L,25L);
        Range r5 = new Range(25L,25L);
        assertTrue(r1.isOverlapping(r1));
        assertTrue(r1.isOverlapping(r2));
        assertTrue(r1.isOverlapping(r3));
        assertTrue(r1.isOverlapping(r4));
        assertFalse(r1.isOverlapping(r5));
    }

    @Test
    void isEmpty() {
        Range r1 = new Range(12L,24L);
        Range r2 = new Range(13L,13L);
        assertFalse(r1.isEmpty());
        assertTrue(r2.isEmpty());
    }

    @Test
    void intersection() {
        Range r1 = new Range(12L,24L);
        Range r2 = new Range(13L,24L);
        Range r3 = new Range(12L,23L);
        Range r4 = new Range(15L,27L);
        Range r5 = new Range(15L,24L);
        assertTrue(r1.intersection(r2).map(r -> r.equals(r2)).orElse(false));
        assertTrue(r1.intersection(r3).map(r -> r.equals(r3)).orElse(false));
        assertTrue(r1.intersection(r4).map(r -> r.equals(r5)).orElse(false));
    }

    @Test
    void union() {
        Range r1 = new Range(12L,24L);
        Range r2 = new Range(13L,24L);
        Range r3 = new Range(12L,23L);
        Range r4 = new Range(15L,27L);
        Range r5 = new Range(12L,27L);
        assertTrue(r1.union(r2).get(0).equals(r1));
        assertTrue(r1.union(r3).get(0).equals(r1));
        assertTrue(r1.union(r4).get(0).equals(r5));
    }

    @Test
    void union2() {
        Range r1 = new Range(12L,24L);
        Range r2 = new Range(24L,27L);
        Range r3 = new Range(12L,27L);
        assertTrue(r1.union(r2).get(0).equals(r3));
    }
}