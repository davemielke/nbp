package org.nbp.b2g.input;

import android.util.Log;

import android.view.accessibility.AccessibilityNodeInfo;

public abstract class ScreenAction extends Action {
  private static final String LOG_TAG = ScreenAction.class.getName();

  protected void logNode (AccessibilityNodeInfo node, String reason) {
    CharSequence text;

    if ((text = node.getText()) == null) {
      if ((text = node.getContentDescription()) == null) {
        text = node.getClassName();
      }
    }

    Log.v(LOG_TAG, reason + ": " + text.toString());
  }

  protected CharSequence getNodeText (AccessibilityNodeInfo node) {
    CharSequence text;

    if ((text = node.getText()) != null) return text;
    if ((text = node.getContentDescription()) != null) return text;
    return null;
  }

  protected AccessibilityNodeInfo getRootNode () {
    ScreenMonitor monitor = getScreenMonitor();
    if (monitor == null) return null;
    return monitor.getRootInActiveWindow();
  }

  public AccessibilityNodeInfo getCurrentNode () {
    AccessibilityNodeInfo root = getRootNode();
    if (root == null) return null;

    AccessibilityNodeInfo current = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
    if (current == null) current = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);

    root.recycle();
    return current;
  }

  protected boolean setCurrentNode (AccessibilityNodeInfo node) {
    if (getNodeText(node) == null) return false;
    return node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
  }

  protected int findChildIndex (AccessibilityNodeInfo parent, AccessibilityNodeInfo node) {
    int count = parent.getChildCount();

    for (int index=0; index<count; index+=1) {
      AccessibilityNodeInfo child = parent.getChild(index);

      if (child != null) {
        boolean found = child.equals(node);
        child.recycle();
        if (found) return index;
      }
    }

    return -1;
  }

  public ScreenAction (String name) {
    super(name);
  }
}
