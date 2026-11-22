package org.example.smith_number;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class SmithSearch {
    static final int MAX_THREADS = 64;

    private SmithSearch() {
    }

    record Range(int first, int last) {
        int size() {
            return last - first + 1;
        }
    }

    static List<Range> ranges(int num, int cnt) {
        if (cnt < 1 || cnt > MAX_THREADS || cnt > num) {
            throw new IllegalArgumentException("Thread count is outside the valid range.");
        }

        long spare = (long) num - cnt;
        int totalWeight = cnt * (cnt + 1) / 2;
        int[] sizes = new int[cnt];
        int previousEnd = 0;
        for (int worker = 1; worker <= cnt; worker++) {
            int cumulativeWeight = worker * (worker + 1) / 2;
            int end = worker + (int) ((spare * cumulativeWeight + totalWeight / 2) / totalWeight);
            sizes[worker - 1] = end - previousEnd;
            previousEnd = end;
        }
        if (cnt > 1 && sizes[0] > 1 && Arrays.stream(sizes).allMatch(size -> size == sizes[0])) {
            sizes[0]--;
            sizes[cnt - 1]++;
        }

        List<Range> ranges = new ArrayList<>(cnt);
        int end = 0;
        for (int size : sizes) {
            ranges.add(new Range(end + 1, end + size));
            end += size;
        }
        return ranges;
    }

    static boolean isSmith(int number) {
        if (number < 4) {
            return false;
        }
        int remaining = number;
        int factorDigitSum = 0;
        for (int factor = 2; (long) factor * factor <= remaining; factor += factor == 2 ? 1 : 2) {
            while (remaining % factor == 0) {
                factorDigitSum += digitSum(factor);
                remaining /= factor;
            }
        }
        if (remaining == number) {
            return false;
        }
        if (remaining > 1) {
            factorDigitSum += digitSum(remaining);
        }
        return factorDigitSum == digitSum(number);
    }

    private static int digitSum(int value) {
        int sum = 0;
        while (value > 0) {
            sum += value % 10;
            value /= 10;
        }
        return sum;
    }
}
