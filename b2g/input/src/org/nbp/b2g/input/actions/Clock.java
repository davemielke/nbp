package org.nbp.b2g.input.actions;
import org.nbp.b2g.input.*;

import android.content.Context;
import android.content.Intent;

public class Clock extends ActivityAction {
  @Override
  protected Intent getIntent (Context context) {
    return new Intent(context, ClockActivity.class);
  }

  public Clock () {
    super();
  }
}
