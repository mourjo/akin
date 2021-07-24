package me.mourjo;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Matcher {

  final Store store;
  final Set<Integer> matchedRows;

  Matcher(String filePath) throws FileNotFoundException {
    store = Reader.read(filePath);
    matchedRows = new HashSet<>(store.getSize() * 2);
  }

  public void exportMatchesToFile(String outputFilePath) {
    var slices = store.getAllSlices();

    try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath)))) {
      int sliceId = 1;
      var matchSlices = computeMatchSlices();
      while(matchSlices.hasNext()) {
        for (var match : matchSlices.next().entrySet()) {
          pw.println(match.getKey().getUUID() + "\t" + match.getValue().getUUID());
        }
        System.out.print("Completed " + sliceId++ + " out of " + slices.size() + " tasks.\r");
      }
      System.out.println("\n");
    } catch (Exception e) {
      e.printStackTrace();
    }
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

  private List<Set<Row>> potentialMatchWindows(Slice slice) {
    List<Set<Row>> windows = new ArrayList<>();

    for (int y = slice.getYear() - 1; y <= slice.getYear() + 1; y++) {
      for (int ln = (int) (0.95 * slice.getLength()); ln <= 1.05 * slice.getLength(); ln++) {
        windows.add(store.lookupRows(y, ln));
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
        continue; // needs to be handled later
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
