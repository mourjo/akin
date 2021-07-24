package me.mourjo;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Matcher {

  final Store store;

  Matcher(String filePath) throws FileNotFoundException {
    store = Reader.read(filePath);
  }

  public void exportMatchesToFile(String outputFilePath) {
    Set<Integer> matchedAlready = new HashSet<>(store.getSize() * 2);
    var slices = store.getAllSlices();

    try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath)))) {
      int sliceId = 1;
      for (Slice slice : slices) {
        for (var match : matchesFor(slice, matchedAlready).entrySet()) {
          pw.println(match.getKey().getUUID() + "\t" + match.getValue().getUUID());
        }
        System.out.print("Completed " + sliceId++ + " out of " + slices.size() + " tasks.\r");
      }
      System.out.println("\n");
    } catch (Exception e) {
      e.printStackTrace();
    }
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

  public Map<Row, Row> matchesFor(Slice slice, Set<Integer> processed) {
    int year = slice.getYear(), len = slice.getLength();
    Map<Row, Row> matches = new HashMap<>();

    for (Row currentRow : store.lookupRows(year, len)) {
      if (processed.contains(currentRow.getId())) {
        continue;
      }
      processed.add(currentRow.getId());
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
          if (!processed.contains(candidate.getId())) {
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
        processed.add(bestMatch.getId());
        matches.putIfAbsent(currentRow, bestMatch);
      }
    }
    return matches;
  }
}
