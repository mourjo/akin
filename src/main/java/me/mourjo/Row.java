package me.mourjo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Row {

  String id;
  int year;
  int len;
  Set<String> genre;
  Set<String> directors;
  Set<String> actors;

  Row(String line) {
    String[] cols = line.split("\t");
    id = cols[0];
    year = Integer.parseInt(cols[1]);
    len = Integer.parseInt(cols[2]);
    genre = splitToSet(cols[3]);
    directors = splitToSet(cols[4]);
    actors = splitToSet(cols[5]);
  }

  private static Set<String> splitToSet(String line) {
    if (line.equals("\\N")) {
      return new HashSet<>();
    }
    var set = new HashSet<String>();
    Collections.addAll(set, line.split(","));
    return set;
  }

  @Override
  public String toString() {
    return "Row{" +
        "id='" + id + '\'' +
        ", year=" + year +
        ", len=" + len +
        ", genre=" + genre +
        ", directors=" + directors +
        ", actors=" + actors +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Row row = (Row) o;
    return id.equals(row.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
