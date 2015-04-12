package org.nbp.b2g.ui.actions;
import org.nbp.b2g.ui.*;

public class LongPressOn extends Action {
  @Override
  public boolean performAction () {
    ApplicationParameters.LONG_PRESS_ENABLED = true;
    return true;
  }

  public LongPressOn (Endpoint endpoint) {
    super(endpoint, false);
  }
}
