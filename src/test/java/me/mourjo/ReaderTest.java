package me.mourjo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReaderTest {

  static Store store;
  static List<Row> allRowsInStore;

  @BeforeAll
  public static void setup() throws FileNotFoundException {
    store = Reader.read("src/main/dev_resources/dedup-2020/movies.tsv");
    if (allRowsInStore == null) {
      List<Row> list = new ArrayList<>();
      for (Slice s : store.getAllSlices()) {
        list.addAll(store.lookupRows(s.getYear(), s.getLength()));
      }
      list.sort(Comparator.comparingInt(o -> -o.getTerms().size()));
      allRowsInStore = Collections.unmodifiableList(list);
    }
  }

  @Test
  public void basic() {

    // cut -f2,3 src/main/resources/dedup-2020/movies.tsv | sort | uniq -c | grep "2016\t90"
    // 1121 2016	90
    assertEquals(1121, store.lookupRows(2016, 90).size());

    // wc -l src/main/resources/dedup-2020/movies.tsv # header extra
    // 558459 src/main/resources/dedup-2020/movies.tsv
    assertEquals(558458, allRowsInStore.size());
  }

  @Test
  public void closestMatchIsItselfOrSubset() throws InterruptedException, ExecutionException {
    // cut -f3 src/main/resources/dedup-2020/movies.tsv | sort | uniq -c | sort -nr | head
    // 27194 90
    // 13916 95
    // 13828 85
    // 13202 80

    var tp = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Future<Row[]>> results = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      var currentRow = allRowsInStore.get(i);
      results.add(tp.submit(() -> findTopMatchAcrossAllRows(currentRow)));
    }

    tp.shutdown();
    assertTrue(tp.awaitTermination(2, TimeUnit.MINUTES),
        "Could not complete running assertions in 2 mins");

    for (Future<Row[]> f : results) {
      Row[] match = f.get();
      assertTrue(areSimilar(match[0], match[1]), "Did not match:\n" + match[0] + "\n" + match[1]);
    }
  }

  public boolean areSimilar(Row row1, Row row2) {
    var commons = new HashSet<>(row1.getTerms());
    commons.retainAll(row2.getTerms());
    return hasEnoughOverlap(commons, row1.getTerms()) && hasEnoughOverlap(commons, row2.getTerms());
  }

  public boolean hasEnoughOverlap(Set<String> s1, Set<String> s2) {
    return 2 * Math.abs((double) s1.size() - s2.size()) / (s1.size() + s2.size()) < 0.4;
  }

  public Row[] findTopMatchAcrossAllRows(Row currentRow) {
    Row matchRow = null;
    double maxScore = Double.NEGATIVE_INFINITY;
    for (Row r : allRowsInStore) {
      double score = store.score(r, currentRow.getTerms());
      if (score > maxScore) {
        matchRow = r;
        maxScore = score;
      }
    }
    return new Row[]{currentRow, matchRow};
  }
}
