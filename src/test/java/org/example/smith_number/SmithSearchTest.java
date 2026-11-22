package org.example.smith_number;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmithSearchTest {
    @Test
    void matchesAndRangesForOneHundred() {
        List<SmithSearch.Range> ranges = SmithSearch.ranges(100, 4);
        assertEquals(List.of(new SmithSearch.Range(1, 11), new SmithSearch.Range(12, 31),
                new SmithSearch.Range(32, 61), new SmithSearch.Range(62, 100)), ranges);

        List<Integer> matches = ranges.stream()
                .flatMapToInt(range -> IntStream.rangeClosed(range.first(), range.last()))
                .filter(SmithSearch::isSmith)
                .boxed().toList();
        assertEquals(List.of(4, 22, 27, 58, 85, 94), matches);
    }

    @Test
    void countsDigitsOfEachRepeatedFactor() {
        assertTrue(SmithSearch.isSmith(121));
        assertTrue(SmithSearch.isSmith(666));
        assertFalse(SmithSearch.isSmith(12));
        assertFalse(SmithSearch.isSmith(1));
        assertFalse(SmithSearch.isSmith(11));
        assertFalse(SmithSearch.isSmith(Integer.MAX_VALUE));
    }

    @Test
    void coversEveryCandidateWithNonemptyRangesAndUnequalSizesWhenPossible() {
        for (int num = 1; num <= 128; num++) {
            for (int cnt = 1; cnt <= Math.min(num, 64); cnt++) {
                checkPartition(num, cnt);
            }
        }
        for (int cnt = 1; cnt <= 64; cnt++) {
            checkPartition(Integer.MAX_VALUE, cnt);
        }
    }

    private void checkPartition(int num, int cnt) {
        List<SmithSearch.Range> ranges = SmithSearch.ranges(num, cnt);
        assertEquals(cnt, ranges.size());
        int last = 0;
        for (SmithSearch.Range range : ranges) {
            assertEquals(last + 1, range.first());
            assertTrue(range.size() > 0);
            last = range.last();
        }
        assertEquals(num, last);
        if (cnt > 1 && num > cnt) {
            assertTrue(ranges.stream().map(SmithSearch.Range::size).distinct().count() > 1);
        }
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> SmithSearch.ranges(0, 1));
        assertThrows(IllegalArgumentException.class, () -> SmithSearch.ranges(10, 0));
        assertThrows(IllegalArgumentException.class, () -> SmithSearch.ranges(3, 4));
        assertThrows(IllegalArgumentException.class, () -> SmithSearch.ranges(100, 65));
    }
}
