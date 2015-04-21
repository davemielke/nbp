package org.nbp.b2g.ui;

import android.content.SharedPreferences;

public abstract class FloatControl extends IntegerControl {
  protected abstract float getLinearScale ();
  protected abstract float getFloatDefault ();
  protected abstract float getFloatValue ();
  protected abstract boolean setFloatValue (float value);

  protected float toFloatValue (float linearValue) {
    return linearValue;
  }

  protected float toLinearValue (float floatValue) {
    return floatValue;
  }

  protected int toIntegerValue (float floatValue) {
    return Math.round(toLinearValue(floatValue) * getLinearScale());
  }

  @Override
  protected final int getIntegerDefault () {
    return toIntegerValue(getFloatDefault());
  }

  @Override
  protected final int getIntegerValue () {
    return toIntegerValue(getFloatValue());
  }

  @Override
  protected final boolean setIntegerValue (int value) {
    return setFloatValue(toFloatValue((float)value / getLinearScale()));
  }

  @Override
  protected void saveValue (SharedPreferences.Editor editor, String key) {
    editor.putFloat(key, getFloatValue());
  }

  @Override
  protected boolean restoreValue (SharedPreferences prefs, String key) {
    return setFloatValue(prefs.getFloat(key, getFloatDefault()));
  }

  protected FloatControl (boolean isForDevelopers) {
    super(isForDevelopers);
  }
}
