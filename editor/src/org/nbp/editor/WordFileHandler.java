package org.nbp.editor;

import android.util.Log;

import org.nbp.common.CommonContext;
import android.content.Context;

import java.io.File;
import android.text.SpannableStringBuilder;

import com.aspose.words.AsposeWordsApplication;
import com.aspose.words.License;

public class WordFileHandler extends FileHandler {
  private final static String LOG_TAG = WordFileHandler.class.getName();

  private enum AsposeState {
    UNSTARTED,
    READY,
    FAILED
  }

  private static AsposeState asposeState = AsposeState.UNSTARTED;

  private final boolean startAspose () {
    synchronized (asposeState) {
      if (asposeState == AsposeState.UNSTARTED) {
        asposeState = AsposeState.FAILED;

        Context context = CommonContext.getContext();
        AsposeWordsApplication app = new AsposeWordsApplication();
        app.loadLibs(context);

        try {
          License license = new License();
          license.setLicense(context.getAssets().open("Aspose.Words.lic"));

          asposeState = AsposeState.READY;
          Log.d(LOG_TAG, "Aspose Words ready");
        } catch (Exception exception) {
          Log.w(LOG_TAG, ("Aspose Words license failure: " + exception.getMessage()));
        }
      }

      return asposeState == AsposeState.READY;
    }
  }

  @Override
  public final void read (File file, SpannableStringBuilder sb) {
    if (startAspose()) {
    }
  }

  @Override
  public final void write (File file, CharSequence text) {
    if (startAspose()) {
    }
  }

  public WordFileHandler () {
    super();
  }
}
