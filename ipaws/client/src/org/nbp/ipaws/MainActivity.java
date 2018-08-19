package org.nbp.ipaws;

import android.util.Log;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

import android.view.View;
import android.widget.Switch;

public class MainActivity extends Activity {
  private final static String LOG_TAG = MainActivity.class.getName();

  private Switch mainSwitch = null;

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);
    mainSwitch = (Switch)findViewById(R.id.main_switch);
  }

  @Override
  protected void onResume () {
    super.onResume();
    mainSwitch.setChecked((AlertService.getAlertService() != null));
  }

  public final void onMainSwitchToggled (View view) {
    boolean isOn = mainSwitch.isChecked();

    if (isOn) {
      startService(AlertService.makeIntent(this));
    } else {
      stopService(AlertService.makeIntent(this));
    }
  }
}
