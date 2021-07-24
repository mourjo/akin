package me.mourjo;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;

public class Utils {

  public static final String asciiFold(String s) {
    return Normalizer.normalize(s.strip().toLowerCase(Locale.ROOT), Form.NFD)
        .replaceAll("(\\p{InCombiningDiacriticalMarks}+)|( )", "");
  }

}
