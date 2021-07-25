package me.mourjo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Reader {

  public static Store read(String filePath) throws FileNotFoundException {
    return read(new FileInputStream(filePath));
  }

  /**
   * Read rows from an input stream and convert it into rows that can be stored for computing match
   * scores.
   *
   * @param stream
   * @return store with all rows
   */
  public static Store read(InputStream stream) {
    final Store store = new Store();
    try {
      var resource = new BufferedReader(new InputStreamReader(stream));
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
