package me.mourjo;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Store {

  private final Map<Integer, Map<Integer, Set<Row>>> yearLenIdx;
  private final Map<String, Integer> termCounts;
  private int numRows = 0;
  private double sumRowLengths = 0d;
  private Set<Slice> allSlices;

  public Store() {
    yearLenIdx = new HashMap<>();
    termCounts = new HashMap<>();
  }

  public int size() {
    return numRows;
  }

  /**
   * Add a row to the store, updating the index to read via slices and additional computations
   * necessary for calculating the score.
   *
   * @param line
   */
  public void addRow(String line) {
    var row = new Row(line);
    numRows++;
    for (String term : row.getTerms()) {
      termCounts.compute(term, (t, c) -> (c == null) ? 1 : c + 1);
    }
    yearLenIdx.putIfAbsent(row.getMovieYear(), new HashMap<>());
    yearLenIdx.get(row.getMovieYear()).putIfAbsent(row.getMovieLength(), new TreeSet<>());
    yearLenIdx.get(row.getMovieYear()).get(row.getMovieLength()).add(row);

    sumRowLengths += row.getTerms().size();
  }

  /**
   * Compute the IDF (inverse document frequency) value for a query qi.
   */
  private double idf(String qi) {
    var nqi = termCounts.getOrDefault(qi, 0);
    return Math.log(1.0 + ((double) numRows - nqi + 0.5) / (nqi + 0.5));
  }

  /**
   * Calculate the rank score according to the BM25 algorithm. See https://en.wikipedia.org/wiki/Okapi_BM25
   *
   * @param row   that is being scored
   * @param query that the row is being scored for
   * @return score for this row
   */
  public double score(Row row, Set<String> query) {
    double result = 0, beta = 0.75, k = 1.2;
    for (String qi : query) {
      var terms = row.getTerms();
      var tf = (terms.contains(qi) ? 1 : 0); // assume terms are not repeated
      double rowLen = terms.size();
      double avgLen = sumRowLengths / numRows;
      result += idf(qi) * (tf * (k + 1)) / (tf + k * (1 - beta + (beta * rowLen / avgLen)));
    }
    return result;
  }

  /**
   * Find rows that with the given year/length combination using the index.
   */
  public Set<Row> lookupRows(int year, int len) {
    var allLengthsForYear = yearLenIdx.get(year);
    if (allLengthsForYear != null) {
      var rows = allLengthsForYear.get(len);
      if (rows != null) {
        return Collections.unmodifiableSet(rows);
      }
    }
    return Set.of();
  }

  /**
   * Returns all slices based on the rows in the store. Memoized for multiple invocations.
   *
   * @return set of slices.
   */
  public Set<Slice> getAllSlices() {
    if (allSlices != null) {
      return allSlices;
    }
    Set<Slice> slices = new HashSet<>();
    for (int thisYear : yearLenIdx.keySet()) {
      for (int thisLength : yearLenIdx.get(thisYear).keySet()) {
        slices.add(Slice.getSlice(thisYear, thisLength));
      }
    }
    allSlices = slices;
    return slices;
  }
}
