package org.nbp.b2g.ui;

import org.nbp.common.speech.SpeechParameters;

public abstract class ApplicationDefaults {
  private ApplicationDefaults () {
  }

  public final static boolean LITERARY_BRAILLE = true;
  public final static BrailleCode BRAILLE_CODE = BrailleCode.EN_UEB_G2;
  public final static boolean WORD_WRAP = true;
  public final static boolean SHOW_NOTIFICATIONS = true;

  public final static TypingMode TYPING_MODE = TypingMode.TEXT;
  public final static boolean TYPING_BOLD = false;
  public final static boolean TYPING_ITALIC = false;
  public final static boolean TYPING_STRIKE = false;
  public final static boolean TYPING_UNDERLINE = false;

  public final static boolean SHOW_HIGHLIGHTED = true;
  public final static IndicatorOverlay SELECTION_INDICATOR = IndicatorOverlay.DOT_8;
  public final static IndicatorOverlay CURSOR_INDICATOR = IndicatorOverlay.DOTS_78;
  public final static GenericLevel BRAILLE_FIRMNESS = GenericLevel.MEDIUM;
  public final static boolean BRAILLE_MONITOR = false;
  public final static boolean BRAILLE_ENABLED = true;

  public final static boolean SPEECH_ENABLED = true;
  public final static float SPEECH_VOLUME = SpeechParameters.VOLUME_MAXIMUM;
  public final static float SPEECH_RATE = SpeechParameters.RATE_REFERENCE;
  public final static float SPEECH_PITCH = SpeechParameters.PITCH_REFERENCE;
  public final static float SPEECH_BALANCE = SpeechParameters.BALANCE_CENTER;
  public final static boolean SLEEP_TALK = false;

  public final static boolean LONG_PRESS = true;
  public final static boolean REVERSE_PANNING = false;

  public final static boolean ONE_HAND = false;
  public final static int SPACE_TIMEOUT = 2000; // milliseconds
  public final static int BINDING_TIMEOUT = 30000; // milliseconds

  public final static boolean REMOTE_DISPLAY = false;
  public final static boolean SECURE_CONNECTION = false;

  public final static boolean CRASH_EMAILS = false;
  public final static boolean ADVANCED_ACTIONS = false;
  public final static boolean EXTRA_INDICATORS = false;
  public final static boolean EVENT_MESSAGES = false;
  public final static boolean LOG_UPDATES = false;
  public final static boolean LOG_KEYBOARD = false;
  public final static boolean LOG_ACTIONS = false;
  public final static boolean LOG_NAVIGATION = false;
  public final static boolean LOG_GESTURES = false;
  public final static boolean LOG_BRAILLE = false;
  public final static boolean LOG_SPEECH = false;
}
