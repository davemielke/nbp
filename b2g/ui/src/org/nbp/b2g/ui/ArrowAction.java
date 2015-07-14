package org.nbp.b2g.ui;

public abstract class ArrowAction extends Action {
  protected boolean performEditAction (Endpoint endpoint) {
    return false;
  }

  protected boolean performSliderAction (Endpoint endpoint) {
    return false;
  }

  protected abstract String getNavigationAction ();

  @Override
  public boolean performAction () {
    Endpoint endpoint = getEndpoint();

    synchronized (endpoint) {
      if (endpoint.isEditable()) {
        return performEditAction(endpoint);
      }

      if (endpoint.isSlider()) {
        return performSliderAction(endpoint);
      }
    }

    return KeyEvents.performAction(getNavigationAction(), getEndpoint());
  }

  protected ArrowAction (Endpoint endpoint, boolean isForDevelopers) {
    super(endpoint, isForDevelopers);
  }
}
