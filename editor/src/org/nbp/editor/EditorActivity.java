package org.nbp.editor;

import java.io.File;

import org.nbp.common.CommonActivity;
import org.nbp.common.FileFinder;

import org.nbp.common.OutgoingMessage;
import android.net.Uri;

import android.util.Log;

import android.content.Context;
import android.content.Intent;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;

import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import android.text.Spanned;
import android.text.SpannableStringBuilder;

import android.view.Menu;
import android.view.MenuItem;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class EditorActivity extends CommonActivity {
  private final static String LOG_TAG = EditorActivity.class.getName();

  private EditText editArea = null;
  private TextView currentPath = null;
  private File currentFile = null;
  private boolean hasChanged = false;

  protected final Activity getActivity () {
    return this;
  }

  private final void showActivityResultCode (int code) {
  }

  private void setCurrentFile (File file, CharSequence content) {
    String path;

    if (file != null) {
      path = file.getAbsolutePath();
    } else {
      path = getString(R.string.message_new_file);
    }

    synchronized (this) {
      currentFile = file;
      currentPath.setText(path);
      editArea.setText(content);
      hasChanged = false;
    }
  }

  private void setCurrentFile () {
    setCurrentFile(null, "");
  }

  private final void saveFile (File file, final Runnable next) {
    CharSequence content;

    synchronized (this) {
      if (file == null) file = currentFile;
      content = editArea.getText();
      hasChanged = false;
    }

    final File f = file;
    final CharSequence c = content;

    new AsyncTask<Void, Void, Void>() {
      AlertDialog dialog;

      @Override
      protected void onPreExecute () {
        dialog = new AlertDialog.Builder(getActivity())
                                .setCancelable(false)
                                .setTitle(R.string.alert_writing_title)
                                .setMessage(f.getAbsolutePath())
                                .create();

        dialog.show();
      }

      @Override
      public Void doInBackground (Void... arguments) {
        FileHandler.get(f).write(f, c);
        return null;
      }

      @Override
      public void onPostExecute (Void result) {
        dialog.dismiss();
        if (next != null) next.run();
      }
    }.execute();
  }

  private final void saveFile (Runnable next) {
    saveFile(null, next);
  }

  private final void saveFile (File file) {
    saveFile(file, null);
  }

  private final void saveFile () {
    saveFile(null, null);
  }

  private final void testHasChanged (final Runnable next) {
    if (hasChanged) {
      OnDialogClickListener positiveListener = new OnDialogClickListener() {
        @Override
        public void onClick () {
          saveFile(next);
        }
      };

      OnDialogClickListener negativeListener = new OnDialogClickListener() {
        @Override
        public void onClick () {
          next.run();
        }
      };

      new AlertDialog.Builder(this)
                     .setTitle(R.string.alert_changed_title)
                     .setMessage(R.string.alert_changed_message)
                     .setPositiveButton(R.string.alert_changed_positive, positiveListener)
                     .setNegativeButton(R.string.alert_changed_negative, negativeListener)
                     .setNeutralButton(R.string.alert_changed_neutral, null)
                     .show();
    } else {
      next.run();
    }
  }

  private final void editFile (final File file) {
    new AsyncTask<Void, Void, CharSequence>() {
      AlertDialog dialog;

      @Override
      protected void onPreExecute () {
        dialog = new AlertDialog.Builder(getActivity())
                                .setCancelable(false)
                                .setTitle(R.string.alert_reading_title)
                                .setMessage(file.getAbsolutePath())
                                .create();

        dialog.show();
      }

      @Override
      protected CharSequence doInBackground (Void... arguments) {
        final SpannableStringBuilder sb = new SpannableStringBuilder();
        FileHandler.get(file).read(file, sb);
        return sb.subSequence(0, sb.length());
      }

      @Override
      protected void onPostExecute (CharSequence content) {
        setCurrentFile(file, content);
        dialog.dismiss();
      }
    }.execute();
  }

  private final void findFile (boolean create, FileFinder.FileHandler handler) {
    File file = currentFile;
    if (file != null) file = file.getParentFile();

    FileFinder.findFile(this, file, create, handler);
  }

  private void menuAction_new () {
    testHasChanged(
      new Runnable() {
        @Override
        public void run () {
          setCurrentFile();
        }
      }
    );
  }

  private void menuAction_open () {
    testHasChanged(
      new Runnable() {
        @Override
        public void run () {
          findFile(false,
            new FileFinder.FileHandler() {
              @Override
              public void handleFile (File file) {
                if (file != null) editFile(file);
              }
            }
          );
        }
      }
    );
  }

  private void menuAction_save () {
    if (currentFile == null) {
      menuAction_saveAs();
    } else {
      saveFile();
    }
  }

  private void menuAction_saveAs () {
    findFile(true,
      new FileFinder.FileHandler() {
        @Override
        public void handleFile (File file) {
          if (file != null) saveFile(file);
        }
      }
    );
  }

  private void menuAction_send () {
    File file = currentFile;

    if (currentFile != null) {
      OutgoingMessage message = new OutgoingMessage();
      message.addAttachment(currentFile);

      if (message.getAttachments().length > 0) {
        if (message.send()) {
        }
      }
    } else {
      showMessage(R.string.alert_send_new_file);
    }
  }

  @Override
  public boolean onOptionsItemSelected (MenuItem item) {
    switch (item.getItemId()) {
      case R.id.options_new:
        menuAction_new();
        return true;

      case R.id.options_open:
        menuAction_open();
        return true;

      case R.id.options_save:
        menuAction_save();
        return true;

      case R.id.options_saveAs:
        menuAction_saveAs();
        return true;

      case R.id.options_send:
        menuAction_send();
        return true;

      default:
        return false;
    }
  }

  @Override
  public boolean onCreateOptionsMenu (Menu menu) {
    getMenuInflater().inflate(R.menu.options, menu);
    return true;
  }

  private final void prepareActionsButton () {
    if (getActionBar() == null) {
      Button button = (Button)findViewById(R.id.actions_button);
      button.setVisibility(button.VISIBLE);

      button.setOnClickListener(
        new Button.OnClickListener() {
          @Override
          public void onClick (View view) {
            editArea.requestFocus();
            getActivity().openOptionsMenu();
          }
        }
      );
    }
  }

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ApplicationContext.setMainActivity(this);

    setContentView(R.layout.editor);
    currentPath = (TextView)findViewById(R.id.current_file);
    editArea = (EditText)findViewById(R.id.edit_area);
    setCurrentFile();

    prepareActionsButton();
    showReportedErrors();
  }

  @Override
  protected void onResume () {
    super.onResume();

    editArea.requestFocus();
  }

  @Override
  protected void onDestroy () {
    try {
    } finally {
      super.onDestroy();
    }
  }
}
