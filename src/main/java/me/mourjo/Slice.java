package me.mourjo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Slice {

  public static Map<Integer, Map<Integer, Slice>> tab = new HashMap<>();
  private final int movieYear;
  private final int movieLength;

  private Slice(int movieYear, int movieLength) {
    this.movieYear = movieYear;
    this.movieLength = movieLength;
  }

  public static Slice getSlice(int year, int length) {
    if (tab.containsKey(year) && tab.get(year).containsKey(length)) {
      return tab.get(year).get(length);
    }
    tab.putIfAbsent(year, new HashMap<>());
    tab.get(year).putIfAbsent(length, new Slice(year, length));
    return tab.get(year).get(length);
  }

  public int getMovieYear() {
    return movieYear;
  }

  public int getMovieLength() {
    return movieLength;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Slice slice = (Slice) o;
    return movieYear == slice.movieYear && movieLength == slice.movieLength;
  }

  @Override
  public int hashCode() {
    return Objects.hash(movieYear, movieLength);
  }

  @Override
  public String toString() {
    return "Slice{" +
        "year=" + movieYear +
        ", length=" + movieLength +
        '}';
  }
}
