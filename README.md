# Smith Number

Smith Number is a JavaFX desktop application that finds every Smith number from 1 through a chosen upper limit. A Smith number is a composite integer whose decimal digits have the same sum as the digits of its prime factors, counting repeated factors. For example, 22 has digit sum 4. Its prime factors are 2 and 11, whose digits also sum to 4. Primes and 1 are excluded.

## Search behavior

The default search covers 1 through 10,000 with four worker threads. Each thread checks a separate, contiguous range and reports its progress. The thread count can be as high as 64, bounded by the upper limit so each worker has at least one number.

A completed search displays the matches in ascending order and reports the Smith, non-Smith, and total counts. Stopping a search cancels its workers and clears partial results.

## Example

For an upper limit of 100 and four threads, the ranges are 1–11, 12–31, 32–61, and 62–100. The six matches are **4, 22, 27, 58, 85, and 94**. The remaining 94 numbers are non-Smith, for a total of 100 checked.
