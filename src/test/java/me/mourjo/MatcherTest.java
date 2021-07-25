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
  static List<Map<Row, Row>> matchSlices = new ArrayList<>();

  static File createTestMovieFile(int lines) throws IOException {
    File tempFile = File.createTempFile("akin_test_", ".tsv");
    try (BufferedReader reader = new BufferedReader(
        new FileReader("src/main/dev_resources/dedup-2020/movies.tsv"));
        PrintWriter writer = new PrintWriter(new FileWriter(tempFile.getAbsolutePath()))) {
      while (lines-- >= 0) {
        writer.println(reader.readLine());
      }
    }
    return tempFile;
  }

  @BeforeAll
  public static void setup() throws IOException {
    File tempFile = createTestMovieFile(100_000);
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