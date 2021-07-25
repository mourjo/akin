package me.mourjo;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Launcher {

  /**
   * Exports matches to a file and prints a progress report while the process is going on.
   *
   * @param matcher
   * @param outputFilePath
   */
  public static void exportMatchesToFile(Matcher matcher, String outputFilePath) {
    var slicesCount = matcher.getSliceCount();

    try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath)))) {
      int sliceId = 1;
      var sliceMatches = matcher.computeSliceMatches();
      while (sliceMatches.hasNext()) {
        for (var match : sliceMatches.next().entrySet()) {
          pw.println(match.getKey().getUUID() + "\t" + match.getValue().getUUID());
        }
        System.out.print("Completed " + sliceId++ + " out of " + slicesCount + " slices.\r");
      }
      System.out.println("\n");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws FileNotFoundException {
    String basePath = "src/main/dev_resources/dedup-2020/";
    String inputFilePath = (args.length >= 1) ? args[0] : basePath + "movies.tsv";
    String outputFilePath = (args.length >= 2) ? args[1] : basePath + "matches.tsv";

    System.out.println();
    exportMatchesToFile(new Matcher(inputFilePath), outputFilePath);
  }

}
