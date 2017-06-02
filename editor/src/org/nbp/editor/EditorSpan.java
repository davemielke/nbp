package org.nbp.editor;

import java.util.Date;

import org.nbp.common.DialogFinisher;
import org.nbp.common.DialogHelper;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;

public abstract class EditorSpan implements DialogFinisher {
  protected EditorSpan () {
  }

  private boolean containsProtectedText = true;

  public final boolean getContainsProtectedText () {
    return containsProtectedText;
  }

  protected final void setContainsProtectedText (boolean yes) {
    containsProtectedText = yes;
  }

  public int getPosition (Spanned content) {
    return content.getSpanStart(this);
  }

  private final static void joinRevisions (Editable content) {
    int length = content.length();
    int start = 0;

    RevisionSpan previousRevision = null;
    final int NO_POSITION = -1;
    int previousStart = NO_POSITION;
    int previousEnd = NO_POSITION;

    while (start != length) {
      int end = content.nextSpanTransition(start, length, RevisionSpan.class);
      RevisionSpan[] nextRevisions = content.getSpans(start, end, RevisionSpan.class);

      if ((nextRevisions != null) && (nextRevisions.length == 1)) {
        RevisionSpan nextRevision = nextRevisions[0];
        int nextStart = content.getSpanStart(nextRevision);
        int nextEnd = content.getSpanEnd(nextRevision);
        boolean isJoinable = false;

      TEST_JOINABILITY:
        {
          if (nextStart != previousEnd) break TEST_JOINABILITY;
          if (nextRevision.getClass() != previousRevision.getClass()) break TEST_JOINABILITY;

          {
            String name = nextRevision.getReviewerName();
            if (name == null) break TEST_JOINABILITY;
            if (!name.equals(previousRevision.getReviewerName())) break TEST_JOINABILITY;
          }

          {
            Date nextTime = nextRevision.getReviewTime();
            if (nextTime == null) break TEST_JOINABILITY;

            Date previousTime = previousRevision.getReviewTime();
            if (previousTime == null) break TEST_JOINABILITY;

            long timeDifference = nextTime.getTime() - previousTime.getTime();
            if (Math.abs(timeDifference) > ApplicationParameters.REVISION_JOIN_MILLISECONDS) break TEST_JOINABILITY;
          }

          isJoinable = true;
        }

        if (isJoinable) {
          {
            Date time = nextRevision.getReviewTime();

            if (previousRevision.getReviewTime().compareTo(time) < 0) {
              previousRevision.setReviewTime(time);
            }
          }

          content.removeSpan(nextRevision);
          content.setSpan(
            previousRevision, previousStart, nextEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
          );

          previousEnd = content.getSpanEnd(previousRevision);
        } else {
          previousRevision = nextRevision;
          previousStart = nextStart;
          previousEnd = nextEnd;
        }
      } else {
        previousRevision = null;
        previousStart = NO_POSITION;
        previousEnd = NO_POSITION;
      }

      start = end;
    }
  }

  public void finishSpan (Editable content) {
  }

  public final static void finishSpans (Editable content) {
    joinRevisions(content);

    for (EditorSpan span : content.getSpans(0, content.length(), EditorSpan.class)) {
      span.finishSpan(content);
    }
  }

  public void restoreSpan (Spannable content) {
  }

  @Override
  public void finishDialog (DialogHelper helper) {
  }
}
