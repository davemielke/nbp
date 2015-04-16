package org.nbp.b2g.ui.actions;
import org.nbp.b2g.ui.*;

public class SpeakRight extends SpeechAction {
  @Override
  public boolean performAction () {
    SpeechDevice speech = getSpeechDevice();

    synchronized (speech) {
      if (Controls.getBalanceControl().up()) {
        return true;
      }
    }

    return false;
  }

  public SpeakRight (Endpoint endpoint) {
    super(endpoint);
  }
}
