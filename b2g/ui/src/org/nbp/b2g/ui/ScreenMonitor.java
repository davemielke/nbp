package org.nbp.b2g.ui;
import org.nbp.b2g.ui.host.HostEndpoint;

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
    return Endpoints.getHostEndpoint();
  }

  @Override
  public void onCreate () {
    super.onCreate();

    Log.d(LOG_TAG, "screen monitor started");
    screenMonitor = this;

    Clipboard.setClipboard(this);
    EventMonitors.startEventMonitors();
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
      } else {
        endpoint.write("no screen content");
      }
    }
  }

  @Override
  public boolean onUnbind (Intent intent) {
    Log.d(LOG_TAG, "screen monitor disconnected");
    return false;
  }

  @Override
  public void onAccessibilityEvent (AccessibilityEvent event) {
    HostEndpoint endpoint = getHostEndpoint();
    int type = event.getEventType();
    List<CharSequence> text = event.getText();
    AccessibilityNodeInfo source = event.getSource();

    if (ApplicationParameters.LOG_ACCESSIBILITY_EVENTS) {
      Log.d(LOG_TAG, "accessibility event: " + event.toString());
    }

    if (source != null) {
      AccessibilityNodeInfo node = ScreenUtilities.getCurrentNode(source);

      if (ApplicationParameters.LOG_ACCESSIBILITY_EVENTS) {
        Log.d(LOG_TAG, "accessibility event source: " + ScreenUtilities.toString(source));
      }

      if (node != null) {
        if (ApplicationParameters.LOG_ACCESSIBILITY_EVENTS) {
          Log.d(LOG_TAG, "accessibility event node: " + ScreenUtilities.toString(node));
        }

        switch (type) {
          case AccessibilityEvent.TYPE_VIEW_FOCUSED:
          case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
          case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            endpoint.write(node, true);
            text = null;
            break;

          default:
            endpoint.write(node, false);
          case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
            text = null;
            break;
        }

        node.recycle();
      } else {
        if (ApplicationParameters.LOG_ACCESSIBILITY_EVENTS) {
          Log.d(LOG_TAG, "no accessibility event node");
        }
      }

      source.recycle();
    } else {
      if (ApplicationParameters.LOG_ACCESSIBILITY_EVENTS) {
        Log.d(LOG_TAG, "no accessibility event source");
      }
    }

    if (text != null) {
      switch (type) {
        default:
          break;
      }

      if (text != null) {
        StringBuilder sb = new StringBuilder();

        for (CharSequence line : text) {
          if (line.length() == 0) continue;
          if (sb.length() > 0) sb.append('\n');
          sb.append(line);
        }

        if (sb.length() > 0) {
          endpoint.write(sb.toString());
        }
      }
    }
  }

  @Override
  public void onInterrupt () {
  }
}
