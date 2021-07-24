package me.mourjo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class Reader {

  public static Store read() {
    Store store = new Store();

    try {
      var resource = new BufferedReader(new InputStreamReader(
          Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
              .getResourceAsStream("dedup-2020/movies.tsv"))));
      String line;
      resource.readLine(); // header

      while ((line = resource.readLine()) != null) {
        store.addRow(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return store;
  }

}
