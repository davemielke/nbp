package org.nbp.b2g.ui;

import android.util.Log;
import android.os.Bundle;
import android.app.Activity;

import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class ControlsActivity extends Activity {
  private final static String LOG_TAG = ControlsActivity.class.getName();

  private View createSaveControlsButton () {
    Button button = new Button(this);
    button.setText(R.string.save_action_label);

    button.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick (View view) {
        Controls.saveControls();
        ApplicationUtilities.message(R.string.save_action_confirmation);
      }
    });

    return button;
  }

  private View createRestoreControlsButton () {
    Button button = new Button(this);
    button.setText(R.string.restore_action_label);

    button.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick (View view) {
        Controls.restoreControls();
        ApplicationUtilities.message(R.string.restore_action_confirmation);
      }
    });

    return button;
  }

  private View createResetControlsButton () {
    Button button = new Button(this);
    button.setText(R.string.reset_action_label);

    button.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick (View view) {
        Controls.resetControls();
        ApplicationUtilities.message(R.string.reset_action_confirmation);
      }
    });

    return button;
  }

  private View createActionsView () {
    LinearLayout view = new LinearLayout(this);
    view.setOrientation(view.HORIZONTAL);

    LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.WRAP_CONTENT,
      LinearLayout.LayoutParams.MATCH_PARENT
    );

    view.addView(createSaveControlsButton(), parameters);
    view.addView(createRestoreControlsButton(), parameters);
    view.addView(createResetControlsButton(), parameters);

    return view;
  }

  private View createNextValueButton (final Control control) {
    Button button = new Button(this);
    button.setText(control.getNextLabel());

    button.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick (View view) {
        if (!control.setNextValue()) {
          ApplicationUtilities.beep();
        }
      }
    });

    return button;
  }

  private View createPreviousValueButton (final Control control) {
    Button button = new Button(this);
    button.setText(control.getPreviousLabel());

    button.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick (View view) {
        if (!control.setPreviousValue()) {
          ApplicationUtilities.beep();
        }
      }
    });

    return button;
  }

  private View createControlLabelView (Control control) {
    TextView view = new TextView(this);
    view.setText(control.getLabel());
    return view;
  }

  private static void setChecked (CompoundButton button, Control control) {
    button.setChecked(((BooleanControl)control).getBooleanValue());
  }

  private View createBooleanValueView (final Control control) {
    final Switch view = new Switch(this);
    setChecked(view, control);

    Switch.OnCheckedChangeListener switchListener = new Switch.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged (CompoundButton button, boolean isChecked) {
        if (isChecked) {
          control.setNextValue();
        } else {
          control.setPreviousValue();
        }
      }
    };

    Control.OnValueChangedListener controlListener = new Control.OnValueChangedListener() {
      @Override
      public void onValueChanged (final Control control) {
        ControlsActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run () {
            setChecked((Switch)view, control);
          }
        });
      }
    };

    view.setOnCheckedChangeListener(switchListener);
    control.addOnValueChangedListener(controlListener);
    return view;
  }

  private View createIntegerValueView (Control control) {
    final TextView view = new TextView(this);
    view.setText(control.getValue());

    Control.OnValueChangedListener controlListener = new Control.OnValueChangedListener() {
      @Override
      public void onValueChanged (final Control control) {
        ControlsActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run () {
            TextView t = (TextView)view;
            t.setText(control.getValue());
          }
        });
      }
    };

    control.addOnValueChangedListener(controlListener);
    return view;
  }

  private View createControlsView () {
    final GridLayout view = new GridLayout(this);
    view.setOrientation(view.VERTICAL);

    Controls.forEachControl(new ControlProcessor() {
      private void setColumn (int row, int column, View content) {
        view.addView(content, new GridLayout.LayoutParams(view.spec(row), view.spec(column)));
      }

      @Override
      public boolean processControl (Control control) {
        int row = view.getRowCount();
        setColumn(row, 0, createControlLabelView(control));

        if (control instanceof BooleanControl) {
          setColumn(row, 1, createBooleanValueView(control));
        } else {
          setColumn(row, 1, createIntegerValueView(control));
          setColumn(row, 2, createPreviousValueButton(control));
          setColumn(row, 3, createNextValueButton(control));
        }

        return true;
      }
    });

    return view;
  }

  private View createRootView () {
    LinearLayout view = new LinearLayout(this);
    view.setOrientation(view.VERTICAL);

     LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    );

    view.addView(createActionsView(), parameters);
    view.addView(createControlsView(), parameters);

    view.setLayoutParams(new ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    ));

    return view;
  }

  @Override
  public void onCreate (Bundle state) {
    super.onCreate(state);
    setContentView(createRootView());
  }
}
