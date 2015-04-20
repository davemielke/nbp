package org.nbp.b2g.ui;

import android.os.Build;

public abstract class ApplicationParameters {
  public final static boolean DEFAULT_ONE_HAND = false;
  public final static boolean DEFAULT_LONG_PRESS = true;

  public final static boolean DEFAULT_SPEECH_ON = true;
  public final static float DEFAULT_SPEECH_VOLUME = 1.0f;
  public final static float DEFAULT_SPEECH_BALANCE = 0.0f;
  public final static float DEFAULT_SPEECH_RATE = 1.0f;
  public final static float DEFAULT_SPEECH_PITCH = 1.0f;

  public static volatile boolean ONE_HAND_MODE = DEFAULT_ONE_HAND;
  public static volatile boolean LONG_PRESS_ACTIONS = DEFAULT_LONG_PRESS;
  public static volatile boolean DEVELOPER_ACTIONS = false;

  public static volatile boolean ENABLE_SPEECH_DEVICE = true;
  public static volatile boolean ENABLE_KEYBOARD_MONITOR = true;
  public static volatile boolean ENABLE_POWER_BUTTON_MONITOR = true;
  public static volatile boolean ENABLE_BLUETOOTH_SERVER = false;

  public static volatile int SCREEN_LEFT_OFFSET = 60; // DIPs
  public static volatile boolean CHORDS_SEND_SYSTEM_KEYS = true;
  public static volatile boolean CHORDS_SEND_ARROW_KEYS = true;

  public static volatile boolean LOG_KEY_EVENTS = false;
  public static volatile boolean LOG_PERFORMED_ACTIONS = false;
  public static volatile boolean LOG_SCREEN_NAVIGATION = false;
  public static volatile boolean LOG_ACCESSIBILITY_EVENTS = false;

  public static volatile long LONG_PRESS_TIME = 500; // milliseconds
  public static volatile long LONG_PRESS_DELAY = 100; // milliseconds
  public static volatile long SCREEN_SCROLL_DELAY = 500; // milliseconds

  public static volatile int BEEP_VOLUME = 100; // percentage
  public static volatile int BEEP_DURATION = 100; // milliseconds

  public static volatile String CLOCK_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss";
  public static volatile long CLOCK_UPDATE_INTERVAL = 1000; // milliseconds

  public static volatile String BLUETOOTH_SERVICE_NAME = "Braille Display";
  public static volatile boolean BLUETOOTH_SECURE_CONNECTION = true;
  public static volatile long BLUETOOTH_RETRY_INTERVAL = 10000; // milliseconds
  public static volatile long BLUETOOTH_READ_TIMEOUT = 1000; // milliseconds

  public static volatile boolean SPEECH_ON = DEFAULT_SPEECH_ON;
  public static volatile long SPEECH_RETRY_DELAY = 5000; // milliseconds

  public static volatile long BRAILLE_MESSAGE_TIME = 2000; // milliseconds
  public static volatile long BRAILLE_REWRITE_DELAY = 50; // milliseconds
  public static volatile int BRAILLE_SCROLL_KEEP = 3; // cells

  public static byte BRAILLE_CHARACTER_UNDEFINED =
    BrailleDevice.DOT_3|
    BrailleDevice.DOT_6|
    BrailleDevice.DOT_7|
    BrailleDevice.DOT_8;

  public static byte BRAILLE_OVERLAY_CURSOR =
    BrailleDevice.DOT_8;

  public static byte BRAILLE_OVERLAY_SELECTED =
    BrailleDevice.DOT_7|
    BrailleDevice.DOT_8;

  public static volatile int SDK_VERSION = Build.VERSION.SDK_INT;

  private ApplicationParameters () {
  }
}
