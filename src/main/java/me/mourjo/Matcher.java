package me.mourjo;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
        try {
          return matchesFor(slices.next());
        } catch (ExecutionException | InterruptedException e) {
          e.printStackTrace();
        }
        return null;
      }
    };
  }

  /**
   * Given a slice, find all rows with the condition, 1 year more or less, length 5% more or less
   *
   * @param slice
   * @return List of rows in all neighbouring slices
   */
  public List<Set<Row>> generateWindows(Slice slice) {
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
  private Map<Row, Row> matchesFor(Slice slice) throws ExecutionException, InterruptedException {
    int movieYear = slice.getMovieYear(), movieLength = slice.getMovieLength();
    Map<Row, Row> matches = new HashMap<>();

    for (Row currentRow : store.lookupRows(movieYear, movieLength)) {
      if (currentRow.getTerms().isEmpty() || matchedRows.contains(currentRow)) {
        continue;
      }

      List<Set<Row>> windows = generateWindows(slice);
      if (windows.isEmpty()) {
        continue;
      }

      var bestMatch = windows.parallelStream()
          .map((Set<Row> window) -> processWindow(window, currentRow))
          .filter(windowResult -> windowResult.row != null)
          .max(Comparator.comparingDouble(windowResult -> windowResult.score));

      if (bestMatch.isPresent()) {
        // no match (maybe only one, or all empty)
        matchedRows.add(currentRow);
        matchedRows.add(bestMatch.get().row);
        matches.put(currentRow, bestMatch.get().row);
      }
    }
    return matches;
  }

  private WindowResult processWindow(Set<Row> window, Row currentRow) {
    Row bestMatch = null;
    double bestScore = Double.NEGATIVE_INFINITY;
    for (Row candidate : window) {
      if (!currentRow.equals(candidate) && !matchedRows.contains(candidate)) {
        double currentScore = store.score(candidate, currentRow.getTerms());
        if (currentScore > bestScore) {
          bestMatch = candidate;
          bestScore = currentScore;
        }
      }
    }
    return new WindowResult(bestMatch, bestScore);
  }

  private static class WindowResult {

    Row row;
    double score;

    WindowResult(Row r, double s) {
      row = r;
      score = s;
    }
  }
}
