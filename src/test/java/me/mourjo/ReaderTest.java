package me.mourjo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReaderTest {

  @Test
  public void basic() {
    var data = Reader.read();

    // cut -f2,3 src/main/resources/dedup-2020/movies.tsv | sort | uniq -c | grep "2016\t90"
    // 1121 2016	90
    assertEquals(1121, data.lookupRows(2016, 90).size());

    // wc -l src/main/resources/dedup-2020/movies.tsv # header extra
    // 558459 src/main/resources/dedup-2020/movies.tsv
    assertEquals(558458, data.getAllRows().size());
  }
}