package org.nbp.b2g.ui;

import android.content.SharedPreferences;

public abstract class BooleanControl extends Control {
  protected abstract boolean getBooleanValue ();
  protected abstract boolean setBooleanValue (boolean value);

  protected boolean getBooleanDefault () {
    return false;
  }

  @Override
  public boolean setNextValue () {
    return setBooleanValue(true);
  }

  @Override
  public boolean setPreviousValue () {
    return setBooleanValue(false);
  }

  @Override
  public boolean setDefaultValue () {
    return setBooleanValue(getBooleanDefault());
  }

  public static String getValue (boolean value) {
    return ApplicationContext.getString(value? R.string.boolean_control_true: R.string.boolean_control_false);
  }

  @Override
  public String getValue () {
    return getValue(getBooleanValue());
  }

  @Override
  protected void saveValue (SharedPreferences.Editor editor, String key) {
    editor.putBoolean(key, getBooleanValue());
  }

  @Override
  protected boolean restoreValue (SharedPreferences prefs, String key) {
    return setBooleanValue(prefs.getBoolean(key, getBooleanDefault()));
  }

  protected BooleanControl () {
    super();
  }
}
