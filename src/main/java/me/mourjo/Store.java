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

  Store() {
    yearLenIdx = new HashMap<>();
    termCounts = new HashMap<>();
  }

  public void addRow(String line) {
    var row = new Row(line);
    numRows++;
    for (String term : row.getTerms()) {
      termCounts.compute(term, (t, c) -> (c == null) ? 1 : c + 1);
    }
    yearLenIdx.putIfAbsent(row.getYear(), new HashMap<>());
    yearLenIdx.get(row.getYear()).putIfAbsent(row.getLen(), new TreeSet<>());
    yearLenIdx.get(row.getYear()).get(row.getLen()).add(row);
    sumRowLengths += row.getTerms().size();
  }

  private double idf(String qi) {
    var nqi = termCounts.getOrDefault(qi, 0);
    return Math.log(1.0 + ((double) numRows - nqi + 0.5) / (nqi + 0.5));
  }

  public double score(Row row, Set<String> query) {
    // See https://en.wikipedia.org/wiki/Okapi_BM25
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

  public Map<Integer, Set<Row>> lookupAllLengthsForYear(int year) {
    return yearLenIdx.get(year);
  }

  public Set<Integer> getAllYears() {
    return yearLenIdx.keySet();
  }

  public Set<Slice> getAllSlices() {
    Set<Slice> slices = new HashSet<>();
    for (int thisYear : yearLenIdx.keySet()) {
      for (int thisLength : yearLenIdx.get(thisYear).keySet()) {
        slices.add(new Slice(thisYear, thisLength));
      }
    }
    return slices;
  }
}
