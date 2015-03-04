package org.nbp.b2g.input;

import android.inputmethodservice.InputMethodService;

public class Action {
  public String getActionName () {
    return "null";
  }

  public boolean performAction () {
    return true;
  }

  protected final InputService inputService;

  public Action (InputService inputService) {
    this.inputService = inputService;
  }
}
