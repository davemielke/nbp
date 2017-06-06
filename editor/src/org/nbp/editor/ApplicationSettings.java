package org.nbp.editor;

public abstract class ApplicationSettings {
  private ApplicationSettings () {
  }

  public volatile static boolean PROTECT_TEXT = ApplicationDefaults.PROTECT_TEXT;
  public volatile static String OWNER_NAME = ApplicationDefaults.OWNER_NAME;
}
