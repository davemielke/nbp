package org.nbp.editor;

public class InsertSpan extends RevisionSpan {
  private final static String decorationPrefix = "⣏⢎";
  private final static String decorationSuffix = "⡱⣹";

  public InsertSpan () {
    super(decorationPrefix, decorationSuffix);
  }

  @Override
  public final int getAction () {
    return R.string.revision_action_insert;
  }
}
