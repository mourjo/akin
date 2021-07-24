package me.mourjo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Row {

  private final String id;
  private final int year;
  private final int len;
  private final Set<String> terms;

  Row(String line) {
    String[] cols = line.split("\t");
    id = cols[0];
    year = Integer.parseInt(cols[1]);
    len = Integer.parseInt(cols[2]);

    // assume terms are not repeated across director, actor, genre
    terms = splitToSet(cols[3]);
    terms.addAll(splitToSet(cols[4]));
    terms.addAll(splitToSet(cols[5]));
  }

  public String getId() {
    return id;
  }

  public int getYear() {
    return year;
  }

  public int getLen() {
    return len;
  }

  public Set<String> getTerms() {
    return Collections.unmodifiableSet(terms);
  }

  private List<String> computeTerms(String line) {
    return Arrays.stream(line.split(",")).map(Utils::asciiFold)
        .collect(Collectors.toList());
  }

  private Set<String> splitToSet(String line) {
    if (line.equals("\\N")) {
      return new HashSet<>();
    }
    return new HashSet<>(computeTerms(line));
  }

  @Override
  public String toString() {
    return "Row{" +
        "id='" + id + '\'' +
        ", year=" + year +
        ", len=" + len +
        ", terms=" + terms +
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
