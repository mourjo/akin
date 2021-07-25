package me.mourjo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Row implements Comparable<Row> {

  private static final AtomicInteger ID_GEN = new AtomicInteger();

  private final int id;
  private final String uuid;
  private final int movieYear;
  private final int movieLength;
  private final Set<String> terms;

  /**
   * Construct a row from a raw line read from a file. Compute relevant auxiliary data necessary for
   * ranking documents. Merge actors, directors and genres into one set of terms to make the ranking
   * process simpler.
   */
  Row(String line) {
    String[] cols = line.split("\t");
    uuid = cols[0];
    id = ID_GEN.incrementAndGet();
    movieYear = Integer.parseInt(cols[1]);
    movieLength = Integer.parseInt(cols[2]);

    // assume terms are not repeated across director, actor, genre
    terms = splitToSet(cols[3]);
    terms.addAll(splitToSet(cols[4]));
    terms.addAll(splitToSet(cols[5]));
  }

  public int getMovieYear() {
    return movieYear;
  }

  public int getMovieLength() {
    return movieLength;
  }

  public Set<String> getTerms() {
    return Collections.unmodifiableSet(terms);
  }

  /**
   * Convert a line into terms by splitting with "," and cleaning up non-ASCII characters.
   */
  private List<String> computeTerms(String line) {
    return Arrays.stream(line.split(",")).map(Utils::asciiFold)
        .collect(Collectors.toList());
  }

  /**
   * Convert a line into a set of terms if it is not empty (\\N)
   */
  private Set<String> splitToSet(String line) {
    if (line.equals("\\N")) {
      return new HashSet<>();
    }
    return new HashSet<>(computeTerms(line));
  }

  @Override
  public String toString() {
    return "Row{" +
        "id=" + id +
        ", year=" + movieYear +
        ", length=" + movieLength +
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
    return id == row.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public int compareTo(Row anotherRow) {
    if (terms.size() == anotherRow.terms.size()) {
      return Integer.compare(id, anotherRow.id);
    }
    return -Integer.compare(terms.size(), anotherRow.terms.size());
  }

  public String getUUID() {
    return uuid;
  }
}
