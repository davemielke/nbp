package org.nbp.b2g.input;

import android.util.Log;

import android.graphics.Rect;

import android.view.accessibility.AccessibilityNodeInfo;

public abstract class ScreenAction extends Action {
  private static final String LOG_TAG = ScreenAction.class.getName();

  protected CharSequence getNodeText (AccessibilityNodeInfo node) {
    CharSequence text;

    if ((text = node.getText()) != null) return text;
    if ((text = node.getContentDescription()) != null) return text;
    return null;
  }

  protected void logNode (AccessibilityNodeInfo node, String reason) {
    CharSequence text = getNodeText(node);
    if (text == null) text = node.getClassName();
    Log.v(LOG_TAG, reason + ": " + text.toString());
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

  protected boolean performNodeAction (AccessibilityNodeInfo node, int action) {
    node = AccessibilityNodeInfo.obtain(node);

    while (node != null) {
      if (node.performAction(action)) return true;
      AccessibilityNodeInfo parent = node.getParent();
      node.recycle();
      node = parent;
    }

    return false;
  }

  protected boolean isActionable (AccessibilityNodeInfo node) {
    if (node.isClickable()) return true;
    if (node.isLongClickable()) return true;
    return false;
  }

  protected boolean hasOuterAction (AccessibilityNodeInfo node) {
    node = AccessibilityNodeInfo.obtain(node);

    do {
      AccessibilityNodeInfo parent = node.getParent();
      node.recycle();

      if (parent == null) return false;
      node = parent;
    } while (!isActionable(node));

    node.recycle();
    return true;
  }

  protected boolean hasInnerAction (AccessibilityNodeInfo node) {
    int childCount = node.getChildCount();

    for (int childIndex=0; childIndex<childCount; childIndex+=1) {
      AccessibilityNodeInfo child = node.getChild(childIndex);

      if (child != null) {
        boolean contains;

        if (isActionable(child)) {
          contains = true;
        } else if (hasInnerAction(child)) {
          contains = true;
        } else {
          contains = false;
        }

        child.recycle();
        if (contains) return true;
      }
    }

    return false;
  }

  protected boolean isVisible (AccessibilityNodeInfo node) {
    if (!node.isVisibleToUser()) return false;

    AccessibilityNodeInfo parent = node.getParent();
    if (parent == null) return true;

    Rect outer = new Rect();
    parent.getBoundsInScreen(outer);
    parent.recycle();
    parent = null;

    Rect inner = new Rect();
    node.getBoundsInScreen(inner);
    return outer.contains(inner);
  }

  protected boolean canSetAsCurrent (AccessibilityNodeInfo node) {
    String name = node.getClassName().toString();
    if (name.equals("android.widget.ListView")) return false;
    return true;
  }

  protected boolean setCurrentNode (AccessibilityNodeInfo node) {
    if (!isActionable(node)) {
      if (getNodeText(node) == null) return false;
      if (hasOuterAction(node)) return false;
    }

    if (!node.isEnabled()) return false;
    if (!canSetAsCurrent(node)) return false;
    if (!isVisible(node)) return false;

    if (!node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)) return false;
    performNodeAction(node, AccessibilityNodeInfo.ACTION_SELECT);
    return true;
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
