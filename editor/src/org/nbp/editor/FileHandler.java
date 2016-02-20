package org.nbp.editor;

import java.util.Map;
import java.util.HashMap;

import java.io.File;

import android.text.SpannableStringBuilder;

public abstract class FileHandler {
  private final static Map<String, FileHandler> map = new HashMap<String, FileHandler>();
  private final static String DEFAULT_EXTENSION = "";

  static {
    map.put(DEFAULT_EXTENSION, new TextFileHandler());

    {
      FileHandler handler = new BrailleFileHandler();
      map.put(".brf", handler);
    }

    {
      FileHandler handler = new AsposeFileHandler();
      map.put(".doc", handler);
      map.put(".docx", handler);
    }
  }

  private static String getExtension (File file) {
    String name = file.getName();
    if (name == null) return null;

    int index = name.lastIndexOf('.');
    if (index < 1) return null;

    String extension = name.substring(index);
    if (extension.isEmpty()) return null;

    return extension;
  }

  public static FileHandler get (File file) {
    String extension = getExtension(file);

    if (extension != null) {
      FileHandler handler = map.get(extension.toLowerCase());
      if (handler != null) return handler;
    }

    return map.get(DEFAULT_EXTENSION);
  }

  public abstract void read (File file, SpannableStringBuilder input);
  public abstract void write (File file, CharSequence output);

  protected FileHandler () {
  }

  public static void readFile (File file, SpannableStringBuilder input) {
    get(file).read(file, input);
  }

  public static void writeFile (File file, CharSequence output) {
    get(file).write(file, output);
  }
}
