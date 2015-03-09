package org.nbp.b2g.input;

import android.util.Log;

import android.view.accessibility.AccessibilityNodeInfo;

public class BackwardAction extends ScreenAction {
  private static final String LOG_TAG = BackwardAction.class.getName();

  private boolean moveToNode (AccessibilityNodeInfo node, int childIndex) {
    if (moveToDescendant(node, childIndex)) return true;
    return setCurrentNode(node);
  }

  private boolean moveToDescendant (AccessibilityNodeInfo node, int childIndex) {
    while (childIndex > 0) {
      AccessibilityNodeInfo child = node.getChild(--childIndex);

      if (child != null) {
        boolean moved = moveToNode(child, child.getChildCount());
        child.recycle();
        if (moved) return true;
      }
    }

    return false;
  }

  @Override
  public final boolean performAction () {
    AccessibilityNodeInfo node = getCurrentNode();

    if (node != null) {
      while (true) {
        AccessibilityNodeInfo parent = node.getParent();
        if (parent == null) break;

        int myChildIndex = findChildIndex(parent, node);
        node.recycle();
        node = parent;
        parent = null;

        if (moveToNode(node, myChildIndex)) {
          node.recycle();
          return true;
        }
      }

      node.recycle();
      node = null;
    }

    return false;
  }

  public BackwardAction () {
    super("backward");
  }
}
