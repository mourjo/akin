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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Matcher {

  final Store store;
  ExecutorService tp;

  Matcher(String filePath) throws FileNotFoundException {
    store = Reader.read(filePath);
    tp = Executors.newWorkStealingPool();
  }

  void exportMatchesToFile(String outputFilePath) {
    List<Future<Map<String, String>>> matchTasks = new ArrayList<>();

    for (Slice s : store.getAllSlices()) {
      matchTasks.add(tp.submit(() -> matchesFor(s)));
    }

    try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath)))) {

      for (var matchTask : matchTasks) {
        for (var match : matchTask.get().entrySet()) {
          pw.println(match.getKey() + "," + match.getValue());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  Map<String, String> matchesFor(Slice s) {
    int year = s.getYear(), len = s.getLength();
    Map<String, String> matches = new HashMap<>();

    for (Row currentRow : store.lookupRows(year, len)) {
      if (currentRow.getTerms().isEmpty()) {
        continue; // needs to be handled later
      }

      List<Set<Row>> windows = new ArrayList<>();

      for (int y : List.of(year - 1, year + 1, year)) {
        for (int ln = (int) (0.95 * len); ln <= 1.05 * len; ln++) {
          windows.add(store.lookupRows(y, ln));
        }
      }

      if (windows.isEmpty()) {
        continue;
      }

      Set<String> matchedAlready = new HashSet<>();
      matchedAlready.add(currentRow.getId());

      Row bestMatch = null;
      double bestScore = Double.NEGATIVE_INFINITY;
      for (Set<Row> window : windows) {
        for (Row r : window) {
          if (!matchedAlready.contains(r.getId())) {
            double currentScore = store.score(r, currentRow.getTerms());
            if (currentScore > bestScore) {
              bestMatch = r;
              bestScore = currentScore;
            }
          }
        }
        if (bestMatch != null) {
          // no match (maybe only one, or all empty)
          matchedAlready.add(bestMatch.getId());
          matches.putIfAbsent(currentRow.getId(), bestMatch.getId());
        }
      }
    }
    return matches;
  }
}
