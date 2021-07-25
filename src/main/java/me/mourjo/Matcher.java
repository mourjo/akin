package me.mourjo;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Matcher {

  private final Store store;
  private final Set<Row> matchedRows;

  public Matcher(String filePath) throws FileNotFoundException {
    store = Reader.read(filePath);
    matchedRows = new HashSet<>(store.size() * 2);
  }

  /**
   * @return Number of slices in the store.
   */
  public int getSliceCount() {
    return store.getAllSlices().size();
  }

  public int getStoreSize() {
    return store.size();
  }

  /**
   * For each slice, find each row's match.
   *
   * @return an iterator over each slice's matches
   */
  public Iterator<Map<Row, Row>> computeSliceMatches() {
    var slices = store.getAllSlices().iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return slices.hasNext();
      }

      @Override
      public Map<Row, Row> next() {
        return matchesFor(slices.next());
      }
    };
  }

  /**
   * Given a slice, find all rows with the condition, 1 year more or less, length 5% more or less
   *
   * @param slice
   * @return List of rows in all neighbouring slices
   */
  public List<Set<Row>> potentialMatchWindows(Slice slice) {
    List<Set<Row>> windows = new ArrayList<>();

    for (int year = slice.getMovieYear() - 1; year <= slice.getMovieYear() + 1; year++) {
      for (int length = (int) (0.95 * slice.getMovieLength());
          length <= Math.ceil(1.05 * slice.getMovieLength());
          length++) {
        windows.add(store.lookupRows(year, length));
      }
    }
    return windows;
  }

  /**
   * For a given slice, find a match for every row in the slice. This requires looking at
   * neighbouring slices that satisfy the condition: 1 year more or less and length 5% more or less
   *
   * @param slice
   * @return map of matchings
   */
  private Map<Row, Row> matchesFor(Slice slice) {
    int movieYear = slice.getMovieYear(), movieLength = slice.getMovieLength();
    Map<Row, Row> matches = new HashMap<>();

    for (Row currentRow : store.lookupRows(movieYear, movieLength)) {
      if (currentRow.getTerms().isEmpty() || matchedRows.contains(currentRow)) {
        continue;
      }

      List<Set<Row>> windows = potentialMatchWindows(slice);
      if (windows.isEmpty()) {
        continue;
      }

      Row bestMatch = null;
      double bestScore = Double.NEGATIVE_INFINITY;
      for (Set<Row> window : windows) {
        for (Row candidate : window) {
          if (!matches.containsKey(candidate) && !currentRow.equals(candidate) &&
              !matchedRows.contains(candidate)) {
            double currentScore = store.score(candidate, currentRow.getTerms());
            if (currentScore > bestScore) {
              bestMatch = candidate;
              bestScore = currentScore;
            }
          }
        }
      }
      if (bestMatch != null) {
        // no match (maybe only one, or all empty)
        matchedRows.add(currentRow);
        matchedRows.add(bestMatch);
        matches.put(currentRow, bestMatch);
      }
    }
    return matches;
  }
}
