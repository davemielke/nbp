package org.nbp.b2g.input.actions;
import org.nbp.b2g.input.*;

import android.view.accessibility.AccessibilityNodeInfo;

public class MoveRight extends MoveForward {
  @Override
  public boolean performAction () {
    if (BrailleDevice.moveRight()) return true;
    return super.performAction();
  }

  public MoveRight () {
    super();
  }
}
