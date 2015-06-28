package org.nbp.b2g.ui;
import org.nbp.b2g.ui.host.HostEndpoint;

import java.util.Collection;
import java.util.List;

import android.util.Log;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityEvent;

import android.content.Intent;

public class ScreenMonitor extends AccessibilityService {
  private final static String LOG_TAG = ScreenMonitor.class.getName();

  private static ScreenMonitor screenMonitor = null;

  public static ScreenMonitor getScreenMonitor () {
    if (screenMonitor == null) Log.w(LOG_TAG, "screen monitor not runnig");
    return screenMonitor;
  }

  private static HostEndpoint getHostEndpoint () {
    return Endpoints.host.get();
  }

  @Override
  public void onCreate () {
    super.onCreate();

    Log.d(LOG_TAG, "screen monitor started");
    screenMonitor = this;

    ApplicationContext.setContext(this);
  }

  @Override
  public void onDestroy () {
    super.onDestroy();
    screenMonitor = null;
    Log.d(LOG_TAG, "screen monitor stopped");
  }

  @Override
  protected void onServiceConnected () {
    Log.d(LOG_TAG, "screen monitor connected");

    {
      HostEndpoint endpoint = getHostEndpoint();
      AccessibilityNodeInfo node = ScreenUtilities.getCurrentNode();

      if (node != null) {
        endpoint.write(node, true);
        node.recycle();
      } else {
        endpoint.write(R.string.message_no_screen_content);
      }
    }
  }

  @Override
  public boolean onUnbind (Intent intent) {
    Log.d(LOG_TAG, "screen monitor disconnected");
    return false;
  }

  private static String toString (Collection<CharSequence> lines) {
    StringBuilder sb = new StringBuilder();

    if (lines != null) {
      for (CharSequence line : lines) {
        if (line.length() == 0) continue;
        if (sb.length() > 0) sb.append('\n');
        sb.append(line);
      }
    }

    if (sb.length() == 0) return null;
    return sb.toString();
  }

  private static void showText (Collection<CharSequence> lines) {
    String string = toString(lines);
    if (string != null) Endpoints.setPopupEndpoint(string);
  }

  private static void appendProperty (StringBuilder sb, String label, String value) {
    if (value != null) {
      if (!value.isEmpty()) {
        if (sb.length() > 0) sb.append('\n');
        sb.append(label);
        sb.append(": ");
        sb.append(value);
      }
    }
  }

  private static void appendProperty (StringBuilder sb, String label, int value) {
    if (value != -1) appendProperty(sb, label, Integer.toString(value));
  }

  private static void appendProperty (StringBuilder sb, String label, Collection<CharSequence> value) {
    appendProperty(sb, label, toString(value));
  }

  private static void say (AccessibilityEvent event) {
    StringBuilder sb = new StringBuilder();

    {
      String string = event.toString();
      int index = string.indexOf(" TYPE_");

      string = string.substring(index+1);
      index = string.indexOf('_');

      string = string.substring(index+1);
      index = string.indexOf(' ');

      string = string.substring(0, index).replace('_', ' ').toLowerCase();
      appendProperty(sb, "Event", string);
    }

    appendProperty(sb, "Count", event.getItemCount());
    appendProperty(sb, "Current", event.getCurrentItemIndex());
    appendProperty(sb, "From", event.getFromIndex());
    appendProperty(sb, "To", event.getToIndex());
    appendProperty(sb, "Text", event.getText());

    if (sb.length() > 0) {
      Devices.speech.get().say(sb.toString());
    }
  }

  private void logMissingEventComponent (String component) {
    if (ApplicationSettings.LOG_UPDATES) {
      Log.d(LOG_TAG, "no accessibility event " + component);
    }
  }

  private void logEventComponent (AccessibilityNodeInfo node, String description) {
    if (ApplicationSettings.LOG_UPDATES) {
      Log.d(LOG_TAG,  String.format(
        "accessibility event %s: %s",
        description, ScreenUtilities.toString(node)
      ));
    }
  }

  private static void setCurrentNode (AccessibilityEvent event) {
    AccessibilityNodeInfo root = event.getSource();

    if (root != null) {
      ScreenUtilities.logNavigation(root, "set event root");

      {
        int childIndex = event.getCurrentItemIndex();

        if (childIndex != -1) {
          int from = event.getFromIndex();
          if (from != -1) childIndex -= from;
        }

        if ((childIndex >= 0) && (childIndex < root.getChildCount())) {
          AccessibilityNodeInfo child = root.getChild(childIndex);

          if (child != null) {
            root.recycle();
            root = child;
            ScreenUtilities.logNavigation(root, "set event child");
          }
        }
      }

      {
        AccessibilityNodeInfo node = ScreenUtilities.findCurrentNode(root);

        if (node != null) {
          ScreenUtilities.logNavigation(node, "set event node");
          ScreenUtilities.setCurrentNode(node);
          node.recycle();
        }
      }

      root.recycle();
    }
  }

  private static void handleViewSelected (AccessibilityEvent event, AccessibilityNodeInfo view) {
    if ((view == null) || ScreenUtilities.isSeekable(view)) {
      int count = event.getItemCount();

      if (count != -1) {
        int index = event.getCurrentItemIndex();
        int percentage =
          (count == 0)? 0:
          (count == 1)? 100:
          ((index * 100) / (count - 1));

        ApplicationUtilities.message("%d%%", percentage);
      }
    } else {
      setCurrentNode(event);
    }
  }

  private static void handleViewScrolled (AccessibilityEvent event, AccessibilityNodeInfo source) {
    ScrollContainer container = ScrollContainer.getContainer(source);

    if (container != null) {
      synchronized (container) {
        container.setItemCount(event.getItemCount());
        container.setFirstItemIndex(event.getFromIndex());
        container.setLastItemIndex(event.getToIndex());
        container.onScroll();
      }
    }
  }

  @Override
  public void onAccessibilityEvent (AccessibilityEvent event) {
    if (ApplicationSettings.LOG_UPDATES) {
      Log.d(LOG_TAG, "accessibility event starting: " + event.toString());
    //say(event);
    }

    try {
      HostEndpoint endpoint = getHostEndpoint();
      int type = event.getEventType();
      AccessibilityNodeInfo source = event.getSource();

      switch (type) {
        case AccessibilityEvent.TYPE_VIEW_FOCUSED:
          setCurrentNode(event);
          break;

        case AccessibilityEvent.TYPE_VIEW_SELECTED:
          handleViewSelected(event, source);
          break;
      }

      if (source != null) {
        logEventComponent(source, "source");
        AccessibilityNodeInfo node = ScreenUtilities.getCurrentNode(source);

        switch (type) {
          case AccessibilityEvent.TYPE_VIEW_SCROLLED:
            handleViewScrolled(event, source);
            break;
        }

        if (node != null) {
          logEventComponent(node, "node");

          switch (type) {
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
              break;

            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
              endpoint.write(node, true);
              break;

            default:
              endpoint.write(node, false);
              break;
          }

          node.recycle();
        } else {
          logMissingEventComponent("node");
        }

        source.recycle();
      } else {
        logMissingEventComponent("source");

        switch (type) {
          case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
            showText(event.getText());
            break;

          default:
            break;
        }
      }
    } catch (Exception exception) {
      Crash.handleCrash(exception, "accessibility event", event.toString());
    }

    if (ApplicationSettings.LOG_UPDATES) {
      Log.d(LOG_TAG, "accessibility event finished");
    }
  }

  @Override
  public void onInterrupt () {
  }
}
