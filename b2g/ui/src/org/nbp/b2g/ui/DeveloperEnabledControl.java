package org.nbp.b2g.ui;

public class DeveloperEnabledControl extends BooleanControl {
  @Override
  public String getLabel () {
    return ApplicationContext.getString(R.string.DeveloperEnabled_control_label);
  }

  @Override
  protected String getPreferenceKey () {
    return "developer-enabled";
  }

  @Override
  protected boolean getBooleanDefault () {
    return ApplicationParameters.DEFAULT_DEVELOPER_ENABLED;
  }

  @Override
  protected boolean getBooleanValue () {
    return ApplicationSettings.DEVELOPER_ENABLED;
  }

  @Override
  protected boolean setBooleanValue (boolean value) {
    ApplicationSettings.DEVELOPER_ENABLED = value;
    return true;
  }

  public DeveloperEnabledControl () {
    super(false);
  }
}
