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
  private final Set<Integer> matchedRows;

  Matcher(String filePath) throws FileNotFoundException {
    store = Reader.read(filePath);
    matchedRows = new HashSet<>(store.getSize() * 2);
  }

  public int getSliceCount() {
    return store.getAllSlices().size();
  }

  public Iterator<Map<Row, Row>> computeMatchSlices() {
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

  public List<Set<Row>> potentialMatchWindows(Slice slice) {
    List<Set<Row>> windows = new ArrayList<>();

    for (int year = slice.getYear() - 1; year <= slice.getYear() + 1; year++) {
      for (int length = (int) (0.95 * slice.getLength());
          length <= Math.ceil(1.05 * slice.getLength());
          length++) {
        windows.add(store.lookupRows(year, length));
      }
    }
    return windows;
  }

  public Map<Row, Row> matchesFor(Slice slice) {
    int year = slice.getYear(), len = slice.getLength();
    Map<Row, Row> matches = new HashMap<>();

    for (Row currentRow : store.lookupRows(year, len)) {
      if (matchedRows.contains(currentRow.getId())) {
        continue;
      }
      matchedRows.add(currentRow.getId());
      if (currentRow.getTerms().isEmpty()) {
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
          if (!matchedRows.contains(candidate.getId())) {
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
        matchedRows.add(bestMatch.getId());
        matches.putIfAbsent(currentRow, bestMatch);
      }
    }
    return matches;
  }
}
