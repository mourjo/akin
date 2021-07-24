package me.mourjo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Store {

  private final Map<Integer, Map<Integer, List<Row>>> index;
  private final List<Row> allRows;

  Store() {
    index = new HashMap<>();
    allRows = new ArrayList<>();
  }

  public void addRow(String line) {
    var row = new Row(line);
    index.putIfAbsent(row.year, new HashMap<>());
    index.get(row.year).putIfAbsent(row.len, new ArrayList<>());
    index.get(row.year).get(row.len).add(row);
    allRows.add(row);
  }

  public List<Row> getAllRows() {
    return Collections.unmodifiableList(allRows);
  }

  public List<Row> lookupRows(int year, int len) {
    var allLengthsForYear = index.get(year);
    if (allLengthsForYear != null) {
      return allLengthsForYear.get(len);
    }
    return null;
  }

  public Map<Integer, List<Row>> lookupAllLengthsForYear(int year) {
    return index.get(year);
  }

  public Set<Integer> getAllYears() {
    return index.keySet();
  }

}
