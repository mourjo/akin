package me.mourjo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
      for (Slice s : store.getAllSlices()){
        list.addAll(store.lookupRows(s.getYear(), s.getLength()));
      }
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
    int currentRowPicker = 10;

    // cut -f3 src/main/resources/dedup-2020/movies.tsv | sort | uniq -c | sort -nr | head
    // 27194 90
    // 13916 95
    // 13828 85
    // 13202 80

    var tp = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Future<Row[]>> results = new ArrayList<>();

    for (int year = 2016; year <= 2020; year++) {
      for (int len = 90; len >= 80; len -= 5) {
        for (int repeater = 0; repeater < 25; repeater++, currentRowPicker += 251) {
          var rowsThisYear = store.lookupRows(year, len);
          var currentRow = rowsThisYear.get(currentRowPicker % rowsThisYear.size());
          if (currentRow.getTerms().isEmpty()) {
            continue; // nothing to match with
          }
          results.add(tp.submit(() -> findTopMatchAcrossAllRows(currentRow)));
        }
      }
    }

    tp.shutdown();
    assertTrue(tp.awaitTermination(2, TimeUnit.MINUTES),
        "Could not complete running assertions in 2 mins");

    for (Future<Row[]> f : results) {
      Row[] match = f.get();
      var firstId = match[0].getId();
      var secondId = match[1].getId();

      var firstTerms = match[0].getTerms();
      var secondTerms = match[1].getTerms();

      assertTrue(firstId.equals(secondId) || firstTerms.containsAll(secondTerms),
          firstId + " did not match " + secondId);
    }
  }

  public Row[] findTopMatchAcrossAllRows(Row currentRow) {
    Row matchRow = allRowsInStore.get(0);
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