package org.nbp.ipaws;

import android.util.Log;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.FileReader;

import java.io.File;
import java.io.FileWriter;

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
  public final static String PROPERTY_SENT = "sent";
  public final static String PROPERTY_EFFECTIVE = "effective";
  public final static String PROPERTY_EXPIRES = "expires";
  public final static String PROPERTY_HEADLINE = "headline";
  public final static String PROPERTY_DESCRIPTION = "description";

  private final static Set<String> PROPERTIES = new HashSet<String>() {
    {
      add(PROPERTY_IDENTIFIER);
      add(PROPERTY_SENT);
      add(PROPERTY_EFFECTIVE);
      add(PROPERTY_EXPIRES);
      add(PROPERTY_HEADLINE);
      add(PROPERTY_DESCRIPTION);
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

              properties.put(name, value);
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
    private final String identifier;
    private final String sent;
    private final String effective;
    private final String expires;
    private final String headline;
    private final String description;

    private Descriptor (Properties properties) {
      identifier = properties.get(PROPERTY_IDENTIFIER);
      sent = properties.get(PROPERTY_SENT);
      effective = properties.get(PROPERTY_EFFECTIVE);
      expires = properties.get(PROPERTY_EXPIRES);
      headline = properties.get(PROPERTY_HEADLINE);
      description = properties.get(PROPERTY_DESCRIPTION);
    }

    public final String getHeadline () {
      return headline;
    }

    @Override
    public void finishDialog (DialogHelper helper) {
      helper.setText(R.id.alert_sent, sent);
      helper.setText(R.id.alert_effective, effective);
      helper.setText(R.id.alert_expires, expires);
      helper.setText(R.id.alert_description, description);
    }
  }

  private final static Map<String, Descriptor> alertCache =
               new HashMap<String, Descriptor>();

  private static File getFile (String identifier) {
    return new File(getAlertsDirectory(), identifier);
  }

  public static Descriptor get (String identifier) {
    synchronized (alertCache) {
      Descriptor descriptor = alertCache.get(identifier);
      if (descriptor != null) return descriptor;

      File file = getFile(identifier);
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

          File permanentFile = getFile(identifier);
          temporaryFile.renameTo(permanentFile);
        } catch (IOException exception) {
          temporaryFile.delete();
          Log.e(LOG_TAG, ("alert file creation error: " + exception.getMessage()));
        }
      }

      synchronized (alertCache) {
        alertCache.put(identifier, new Descriptor(properties));
      }
    }
  }

  public static void remove (String identifier) {
    File file = getFile(identifier);
    file.delete();

    synchronized (alertCache) {
      alertCache.remove(identifier);
    }
  }

  public static String[] list () {
    return getAlertsDirectory().list();
  }
}
