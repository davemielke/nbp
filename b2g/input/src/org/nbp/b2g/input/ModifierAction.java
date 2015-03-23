package org.nbp.b2g.input;

public abstract class ModifierAction extends Action {
  private boolean modifierState = false;

  public boolean getState () {
    boolean state = modifierState;
    modifierState = false;
    return state;
  }

  @Override
  public final boolean performAction () {
    modifierState = true;
    return true;
  }

  protected ModifierAction () {
    super();
  }
}
