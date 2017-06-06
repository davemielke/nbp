package org.nbp.editor.menu.review;
import org.nbp.editor.*;

public class PreviousChange extends EditorAction {
  public PreviousChange (EditorActivity editor) {
    super(editor);
  }

  @Override
  public void performAction (EditorActivity editor) {
    if (!editor.getEditArea().moveToPreviousChange()) {
      editor.showMessage(R.string.message_no_previous_change);
    }
  }
}
