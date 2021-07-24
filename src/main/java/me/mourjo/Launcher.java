package me.mourjo;

import java.io.FileNotFoundException;

public class Launcher {

  public static void main(String[] args) throws FileNotFoundException {
    String inputFilePath =
        (args.length >= 1) ? args[0] : "src/main/dev_resources/dedup-2020/movies.tsv";
    String outputFilePath = (args.length >= 2) ? args[1] : "/tmp/matches.csv";

    var matcher = new Matcher(inputFilePath);
    matcher.exportMatchesToFile(outputFilePath);
  }

}
