package org.nbp.editor;

import android.text.SpannableStringBuilder;

public class BrailleFileHandler extends TextFileHandler {
  @Override
  protected void postProcessInput (SpannableStringBuilder input) {
    int length = input.length();
    int start = -1;
    StringBuilder sb = new StringBuilder();

    for (int index=0; index<length; index+=1) {
      char character = input.charAt(index);

      if ((character >= 0X40) && (character < 0X5F)) {
        if (start < 0) start = index;
        sb.append((char)(character + 0X20));
      } else if (start >= 0) {
        input.replace(start, index, sb.toString());
        sb.delete(0, sb.length());
        start = -1;
      }
    }

    if (start >= 0) input.replace(start, length, sb.toString());
  }

  public BrailleFileHandler () {
    super();
  }
}
