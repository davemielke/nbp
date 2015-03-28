package org.nbp.b2g.input;

import android.util.Log;

public class KeyboardMonitor extends Thread {
  private static final String LOG_TAG = KeyboardMonitor.class.getName();

  private static KeyboardMonitor keyboardMonitor = null;
  private final static Object keyboardMonitorStartLock = new Object();

  public static KeyboardMonitor getKeyboardMonitor () {
    KeyboardMonitor monitor;

    synchronized (keyboardMonitorStartLock) {
      monitor = keyboardMonitor;
    }

    if (monitor == null) Log.w(LOG_TAG, "keyboard monitor not running");
    return monitor;
  }

  public static boolean isActive () {
    KeyboardMonitor monitor = getKeyboardMonitor();

    return (monitor != null)? monitor.isAlive(): false;
  }

  private static native boolean openKeyboard ();
  private static native void closeKeyboard ();
  private static native void monitorKeyboard (KeyboardMonitor monitor);

  public void onKeyEvent (int code, boolean press) {
    if (ApplicationParameters.LOG_KEY_EVENTS) {
      Log.d(LOG_TAG, "key " + (press? "press": "release") + ": " + code);
    }

    KeyEvents.handleKeyEvent(ScanCode.toKeyMask(code), press);
  }

  public void run () {
    Log.d(LOG_TAG, "keyboard monitor started");

    if (openKeyboard()) {
      KeyEvents.resetKeys();
      monitorKeyboard(this);
      closeKeyboard();
    } else {
      Log.w(LOG_TAG, "keyboard device not opened");
    }

    Log.d(LOG_TAG, "keyboard monitor stopped");

    synchronized (keyboardMonitorStartLock) {
      keyboardMonitor = null;
    }
  }

  public static boolean startKeyboardMonitor () {
    if (ApplicationParameters.START_KEYBOARD_MONITOR) {
      synchronized (keyboardMonitorStartLock) {
        if (keyboardMonitor == null) {
          keyboardMonitor = new KeyboardMonitor();
          keyboardMonitor.start();
        } else {
          Log.w(LOG_TAG, "keyboard monitor already running");
        }
      }

      return true;
    }

    return false;
  }

  public KeyboardMonitor () {
    super("B2G-keyboard-monitor");
  }

  static {
    System.loadLibrary("InputService");
  }
}
