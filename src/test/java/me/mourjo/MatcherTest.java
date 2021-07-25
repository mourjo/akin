package me.mourjo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MatcherTest {

  static Matcher matcher;
  static List<Map<Row, Row>> matchSlices;

  static void fillTestMovieFile(File f, int lines) throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new FileReader("src/main/dev_resources/dedup-2020/movies.tsv"));
        PrintWriter writer = new PrintWriter(new FileWriter(f.getAbsolutePath()))) {
      while (lines-- >= 0) {
        writer.println(reader.readLine());
      }
    }
  }

  @BeforeAll
  public static void setup() throws IOException {
    File tempFile = File.createTempFile("akin_test_", ".tsv");
    fillTestMovieFile(tempFile, 100_000);
    matchSlices = new ArrayList<>();
    matcher = new Matcher(tempFile.getAbsolutePath());
    var it = matcher.computeSliceMatches();
    while (it.hasNext()) {
      matchSlices.add(it.next());
    }
  }

  @Test
  public void ensureNoDuplicateMatches() {
    Set<Row> leftRows = new HashSet<>();
    Set<Row> rightRows = new HashSet<>();
    int count = 0;
    for (Map<Row, Row> sliceResult : matchSlices) {
      count += sliceResult.size();
      leftRows.addAll(sliceResult.keySet());
      rightRows.addAll(sliceResult.values());
    }
    assertEquals(leftRows.size(), rightRows.size());
    assertEquals(leftRows.size(), count);
  }

  @Test
  public void ensureMostRowsAreMatched() {
    int count = 0;
    for (Map<Row, Row> sliceResult : matchSlices) {
      count += sliceResult.size();
    }
    assertTrue(((matcher.getStoreSize() - (count * 2d)) / matcher.getStoreSize()) < 0.01);
  }

}