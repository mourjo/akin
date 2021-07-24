package me.mourjo;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Launcher {

  public static void exportMatchesToFile(Matcher matcher, String outputFilePath) {
    var slicesCount = matcher.getSliceCount();

    try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath)))) {
      int sliceId = 1;
      var matchSlices = matcher.computeMatchSlices();
      while (matchSlices.hasNext()) {
        for (var match : matchSlices.next().entrySet()) {
          pw.println(match.getKey().getUUID() + "\t" + match.getValue().getUUID());
        }
        System.out.print("Completed " + sliceId++ + " out of " + slicesCount + " tasks.\r");
      }
      System.out.println("\n");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws FileNotFoundException {
    String inputFilePath =
        (args.length >= 1) ? args[0] : "src/main/dev_resources/dedup-2020/movies.tsv";
    String outputFilePath = (args.length >= 2) ? args[1] : "/tmp/matches.csv";

    var matcher = new Matcher(inputFilePath);
    exportMatchesToFile(matcher, outputFilePath);
  }

}
