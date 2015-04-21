package org.nbp.b2g.ui;

import android.content.SharedPreferences;

public abstract class BooleanControl extends Control {
  protected abstract boolean getBooleanValue ();
  protected abstract boolean setBooleanValue (boolean value);

  protected boolean getBooleanDefault () {
    return false;
  }

  private final boolean setValue (boolean value) {
    if (value == getBooleanValue()) return false;
    return setBooleanValue(value);
  }

  @Override
  protected final boolean setNextValue () {
    return setValue(true);
  }

  @Override
  protected final boolean setPreviousValue () {
    return setValue(false);
  }

  @Override
  protected final boolean setDefaultValue () {
    return setBooleanValue(getBooleanDefault());
  }

  public static String getValue (boolean value) {
    return ApplicationContext.getString(value?
           R.string.boolean_control_next:
           R.string.boolean_control_previous);
  }

  @Override
  public String getValue () {
    return getValue(getBooleanValue());
  }

  @Override
  public String getNextLabel () {
    return getValue(true);
  }

  @Override
  public String getPreviousLabel () {
    return getValue(false);
  }

  @Override
  protected void saveValue (SharedPreferences.Editor editor, String key) {
    editor.putBoolean(key, getBooleanValue());
  }

  @Override
  protected boolean restoreValue (SharedPreferences prefs, String key) {
    return setBooleanValue(prefs.getBoolean(key, getBooleanDefault()));
  }

  protected BooleanControl (boolean isForDevelopers) {
    super(isForDevelopers);
  }
}
