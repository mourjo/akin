package me.mourjo;

public class Launcher {

  public static void main(String[] args) {
    var data = Reader.read();

    System.out.println(data.lookupRows(2016, 90).size());
    System.out.println(data.getAllRows().size());

  }

}
