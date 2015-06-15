package org.nbp.b2g.ui.host.actions;
import org.nbp.b2g.ui.host.*;
import org.nbp.b2g.ui.*;

public class AndroidAccessibilitySettings extends SystemActivityAction {
  @Override
  protected String getIntentAction () {
    return android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS;
  }

  public AndroidAccessibilitySettings (Endpoint endpoint) {
    super(endpoint, false);
  }
}
