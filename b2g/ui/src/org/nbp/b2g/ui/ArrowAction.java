package org.nbp.b2g.ui;

public abstract class ArrowAction extends ScanCodeAction {
  protected boolean performEditAction (Endpoint endpoint) {
    return false;
  }

  protected boolean performSeekAction (Endpoint endpoint) {
    return false;
  }

  @Override
  public boolean performAction () {
    Endpoint endpoint = getEndpoint();

    synchronized (endpoint) {
      if (endpoint.isEditable()) {
        return performEditAction(endpoint);
      }

      if (endpoint.isSeekable()) {
        return performSeekAction(endpoint);
      }
    }

    return super.performAction();
  }

  protected int getArrowKeyCode () {
    return NULL_KEY_CODE;
  }

  @Override
  protected int getKeyCode () {
    if (ApplicationParameters.CHORDS_SEND_ARROW_KEYS && isChord()) return NULL_KEY_CODE;
    return getArrowKeyCode();
  }

  protected ArrowAction (Endpoint endpoint, boolean isForDevelopers) {
    super(endpoint, isForDevelopers);
  }
}
