package me.mourjo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Slice {

  public static Map<Integer, Map<Integer, Slice>> tab = new HashMap<>();
  private final int year;
  private final int length;

  private Slice(int year, int length) {
    this.year = year;
    this.length = length;
  }

  public static Slice getSlice(int year, int length) {
    if (tab.containsKey(year) && tab.get(year).containsKey(length)) {
      return tab.get(year).get(length);
    }
    tab.putIfAbsent(year, new HashMap<>());
    tab.get(year).putIfAbsent(length, new Slice(year, length));
    return tab.get(year).get(length);
  }

  public int getYear() {
    return year;
  }

  public int getLength() {
    return length;
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
    return year == slice.year && length == slice.length;
  }

  @Override
  public int hashCode() {
    return Objects.hash(year, length);
  }

  @Override
  public String toString() {
    return "Slice{" +
        "year=" + year +
        ", length=" + length +
        '}';
  }
}
