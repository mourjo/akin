package me.mourjo;

import java.util.Objects;

public class Slice {
  private final int year;
  private final int length;

  Slice(int year, int length) {
    this.year = year;
    this.length = length;
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
