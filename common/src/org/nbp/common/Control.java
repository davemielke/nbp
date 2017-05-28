package org.nbp.common;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;
import java.util.HashSet;

public abstract class Control {
  protected Control () {
  }

  protected abstract int getResourceForGroup ();
  protected abstract int getResourceForLabel ();

  public abstract CharSequence getValue ();
  protected abstract boolean setDefaultValue ();
  protected abstract boolean setNextValue ();
  protected abstract boolean setPreviousValue ();

  protected abstract void saveValue (SharedPreferences.Editor editor, String key);
  protected abstract boolean restoreValue (SharedPreferences prefs, String key);

  protected final String getString (int resource) {
    return CommonContext.getString(resource);
  }

  public final String getGroup () {
    return getString(getResourceForGroup());
  }

  public final String getLabel () {
    return getString(getResourceForLabel());
  }

  protected int getResourceForNext () {
    return R.string.control_next_default;
  }

  public String getLabelForNext () {
    return getString(getResourceForNext());
  }

  protected int getResourceForPrevious () {
    return R.string.control_previous_default;
  }

  public String getLabelForPrevious () {
    return getString(getResourceForPrevious());
  }

  public interface ValueConfirmationListener {
    public abstract void confirmValue (String confirmation);
  }

  private static ValueConfirmationListener valueConfirmationListener = null;

  public final static ValueConfirmationListener getValueConfirmationListener () {
    return valueConfirmationListener;
  }

  public final static void setValueConfirmationListener (ValueConfirmationListener listener) {
    valueConfirmationListener = listener;
  }

  protected String getValueConfirmation () {
    return getLabel() + " " + getValue();
  }

  public final void confirmValue () {
    if (valueConfirmationListener != null) {
      valueConfirmationListener.confirmValue(getValueConfirmation());
    }
  }

  public interface OnValueChangedListener {
    public abstract void onValueChanged (Control control);
  }

  private final Set<OnValueChangedListener> onValueChangedListeners =
        new HashSet<OnValueChangedListener>();

  public final boolean addOnValueChangedListener (OnValueChangedListener listener) {
    return onValueChangedListeners.add(listener);
  }

  public final boolean removeOnValueChangedListener (OnValueChangedListener listener) {
    return onValueChangedListeners.remove(listener);
  }

  protected String getPreferenceKey () {
    return null;
  }

  private final static SharedPreferences getSettings (String name) {
    return CommonContext.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
  }

  private final boolean saveValue (SharedPreferences prefs) {
    String key = getPreferenceKey();
    if (key == null) return true;

    SharedPreferences.Editor editor = prefs.edit();
    saveValue(editor, key);
    return editor.commit();
  }

  private final static SharedPreferences getCurrentSettings () {
    return getSettings("current-settings");
  }

  protected final void reportValue (boolean confirm) {
    saveValue(getCurrentSettings());
    if (confirm) confirmValue();

    for (OnValueChangedListener listener : onValueChangedListeners) {
      listener.onValueChanged(this);
    }
  }

  public final boolean restoreDefaultValue () {
    if (!setDefaultValue()) return false;
    reportValue(false);
    return true;
  }

  private final boolean restoreValue (SharedPreferences prefs) {
    String key = getPreferenceKey();
    if (key == null) return restoreDefaultValue();

    if (!restoreValue(prefs, key)) return false;
    reportValue(false);
    return true;
  }

  public final boolean restoreCurrentValue () {
    return restoreValue(getCurrentSettings());
  }

  private final static SharedPreferences getSavedSettings () {
    return getSettings("saved-settings");
  }

  public final boolean saveValue () {
    return saveValue(getSavedSettings());
  }

  public final boolean restoreSavedValue () {
    return restoreValue(getSavedSettings());
  }

  public final boolean nextValue () {
    if (!setNextValue()) return false;
    reportValue(true);
    return true;
  }

  public final boolean previousValue () {
    if (!setPreviousValue()) return false;
    reportValue(true);
    return true;
  }
}
