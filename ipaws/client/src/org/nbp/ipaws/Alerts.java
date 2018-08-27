package org.nbp.ipaws;

import android.util.Log;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.FileReader;

import java.io.File;
import java.io.FileWriter;

import java.util.Arrays;
import java.util.Comparator;

import java.util.Map;
import java.util.HashMap;

import java.util.Set;
import java.util.HashSet;

import org.nbp.common.DialogFinisher;
import org.nbp.common.DialogHelper;

public abstract class Alerts extends ApplicationComponent {
  private final static String LOG_TAG = Alerts.class.getName();

  private Alerts () {
    super();
  }

  public final static String PROPERTY_IDENTIFIER = "identifier";
  public final static String PROPERTY_SENDER = "senderName";
  public final static String PROPERTY_SENT = "sent";
  public final static String PROPERTY_EFFECTIVE = "effective";
  public final static String PROPERTY_ONSET = "onset";
  public final static String PROPERTY_EXPIRES = "expires";
  public final static String PROPERTY_EVENT = "event";
  public final static String PROPERTY_AREA = "areaDesc";
  public final static String PROPERTY_HEADLINE = "headline";
  public final static String PROPERTY_DESCRIPTION = "description";
  public final static String PROPERTY_INSTRUCTION = "instruction";
  public final static String PROPERTY_URI = "uri";

  private final static Set<String> PROPERTIES = new HashSet<String>() {
    {
      add(PROPERTY_IDENTIFIER);
      add(PROPERTY_SENDER);
      add(PROPERTY_SENT);
      add(PROPERTY_EFFECTIVE);
      add(PROPERTY_ONSET);
      add(PROPERTY_EXPIRES);
      add(PROPERTY_EVENT);
      add(PROPERTY_AREA);
      add(PROPERTY_HEADLINE);
      add(PROPERTY_DESCRIPTION);
      add(PROPERTY_INSTRUCTION);
      add(PROPERTY_URI);
    }
  };

  private static class Properties extends HashMap<String, String> {
    private Properties () {
      super();
    }
  }

  private static Properties getProperties (Reader reader) {
    XmlPullParser parser = Xml.newPullParser();
    Properties properties = new Properties();

    try {
      parser.setInput(reader);

      while (true) {
        switch (parser.next()) {
          case XmlPullParser.END_DOCUMENT:
            return properties;

          case XmlPullParser.START_TAG: {
            String name = parser.getName();

            if (PROPERTIES.contains(name)) {
              parser.next();
              parser.require(XmlPullParser.TEXT, null, null);
              String value = parser.getText();

              parser.next();
              parser.require(XmlPullParser.END_TAG, null, name);

              if (properties.get(name) == null) properties.put(name, value);
            }

            break;
          }
        }
      }
    } catch (XmlPullParserException exception) {
      Log.e(LOG_TAG, ("XML error: " + exception.getMessage()));
    } catch (IOException exception) {
      Log.e(LOG_TAG, ("I/O error: " + exception.getMessage()));
    }

    return null;
  }

  private static Properties getProperties (String xml) {
    return getProperties(new StringReader(xml));
  }

  public static class Descriptor implements DialogFinisher {
    private final static String XML_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
    private final static DateFormat xmlTimeFormatter =
      new SimpleDateFormat(XML_TIME_FORMAT
      );

    private Date parseTime (String time) {
      if (time != null) {
        try {
          return xmlTimeFormatter.parse(time);
        } catch (ParseException exception) {
          Log.w(LOG_TAG, ("time conversion error: " + time + ": " + exception.getMessage()));
        }
      }

      return null;
    }

    private final String identifier;
    private final String sender;
    private final Date sent;
    private final Date effective;
    private final Date onset;
    private final Date expires;
    private final String event;
    private final String area;
    private final String headline;
    private final String description;
    private final String instruction;
    private final String uri;

    private Descriptor (Properties properties) {
      identifier = properties.get(PROPERTY_IDENTIFIER);
      sender = properties.get(PROPERTY_SENDER);
      sent = parseTime(properties.get(PROPERTY_SENT));

      {
        Date time = parseTime(properties.get(PROPERTY_EFFECTIVE));
        if (time == null) time = sent;
        effective = time;
      }

      onset = parseTime(properties.get(PROPERTY_ONSET));

      {
        Date time = parseTime(properties.get(PROPERTY_EXPIRES));
        expires = time;
      }

      event = properties.get(PROPERTY_EVENT);
      area = properties.get(PROPERTY_AREA);
      headline = properties.get(PROPERTY_HEADLINE);
      description = properties.get(PROPERTY_DESCRIPTION);
      instruction = properties.get(PROPERTY_INSTRUCTION);
      uri = properties.get(PROPERTY_URI);
    }

    public final String getSummary () {
      String[] strings = new String[] {headline, event};

      for (String string : strings) {
        if (string != null) {
          if (!string.isEmpty()) {
            return string;
          }
        }
      }

      return identifier;
    }

    private final static DateFormat timeFormatter =
      new SimpleDateFormat(
        ApplicationParameters.TIME_FORMAT
      );

    private final String formatTime (Date date) {
      if (date == null) return null;
      return timeFormatter.format(date);
    }

    @Override
    public void finishDialog (DialogHelper helper) {
      helper.setText(R.id.alert_sender, sender);
      helper.setText(R.id.alert_sent, formatTime(sent));
      helper.setText(R.id.alert_effective, formatTime(effective));
      helper.setText(R.id.alert_onset, formatTime(onset));
      helper.setText(R.id.alert_expires, formatTime(expires));
      helper.setText(R.id.alert_event, event);
      helper.setText(R.id.alert_area, area);
      helper.setText(R.id.alert_description, description);
      helper.setText(R.id.alert_instruction, instruction);
    }
  }

  private final static Map<String, Descriptor> alertCache =
               new HashMap<String, Descriptor>();

  private static File getAlertsDirectory () {
    return getFilesDirectory("alerts");
  }

  private static File getAlertFile (String identifier) {
    return new File(getAlertsDirectory(), identifier);
  }

  public static Descriptor get (String identifier) {
    synchronized (alertCache) {
      Descriptor descriptor = alertCache.get(identifier);
      if (descriptor != null) return descriptor;

      File file = getAlertFile(identifier);
      Properties properties = null;

      try {
        properties = getProperties(new FileReader(file));
      } catch (IOException exception) {
        Log.e(LOG_TAG, ("alert read error: " + exception.getMessage()));
      }

      if (properties == null) return null;
      descriptor = new Descriptor(properties);
      alertCache.put(identifier, descriptor);
      return descriptor;
    }
  }

  public static void add (String identifier, String xml) {
    Properties properties = getProperties(xml);

    if (properties != null) {
      if (!identifier.isEmpty()) {
        properties.put(PROPERTY_IDENTIFIER, identifier);
      }

      {
        File temporaryFile = new File(getFilesDirectory(), "new-alert");

        try {
          FileWriter writer = new FileWriter(temporaryFile);
          writer.write(xml);
          writer.close();
          temporaryFile.setReadOnly();

          File permanentFile = getAlertFile(identifier);
          temporaryFile.renameTo(permanentFile);
        } catch (IOException exception) {
          temporaryFile.delete();
          Log.e(LOG_TAG, ("alert file creation error: " + exception.getMessage()));
        }
      }

      Descriptor alert = new Descriptor(properties);

      {
        String uri = alert.uri;
        AlertPlayer.play(uri, true);

        if ((uri == null) || uri.isEmpty()) {
          String description = alert.description;

          if ((description != null) && !description.isEmpty()) {
            Announcements.add(identifier, description);
          }
        }
      }

      synchronized (alertCache) {
        alertCache.put(identifier, alert);
      }
    }
  }

  public static void remove (String identifier) {
    Announcements.remove(identifier);

    File file = getAlertFile(identifier);
    file.delete();

    synchronized (alertCache) {
      alertCache.remove(identifier);
    }
  }

  public static String[] list (boolean sorted) {
    File directory = getAlertsDirectory();
    String[] names = directory.list();

    if (names.length > 1) {
      if (sorted) {
        final Map<String, Long> times = new HashMap<String, Long>();

        for (String name : names) {
          times.put(name, new File(directory, name).lastModified());
        }

        Arrays.sort(names,
          new Comparator<String>() {
            @Override
            public int compare (String name1, String name2) {
              return times.get(name2).compareTo(times.get(name1));
            }
          }
        );
      }
    }

    return names;
  }
}
