package org.nbp.b2g.ui.host;
import org.nbp.b2g.ui.*;

import org.nbp.common.CommonSettingsActivity;
import org.nbp.common.LaunchUtilities;

import android.view.View;
import android.widget.Button;

public class SettingsActivity extends CommonSettingsActivity {
  private final static String LOG_TAG = SettingsActivity.class.getName();

  public SettingsActivity () {
    super(Controls.getAllControls());
  }

  private View createSystemMaintenanceButton () {
    Button button = newButton(
      R.string.SystemMaintenance_action_label,
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          LaunchUtilities.launchActivity(MaintenanceActivity.class);
        }
      }
    );

    return button;
  }
}
