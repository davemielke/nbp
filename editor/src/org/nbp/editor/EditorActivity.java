package org.nbp.editor;

import java.io.File;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;

import java.util.Set;
import java.util.LinkedHashSet;

import org.nbp.common.CommonActivity;
import org.nbp.common.CommonUtilities;

import org.nbp.common.AlertDialogBuilder;
import org.nbp.common.DialogFinisher;
import org.nbp.common.DialogHelper;

import org.nbp.common.LanguageUtilities;
import java.lang.reflect.Constructor;

import org.nbp.common.FileFinder;
import org.nbp.common.FileSystems;

import android.net.Uri;

import android.util.Log;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.AsyncTask;

import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import android.widget.TableLayout;
import android.widget.TableRow;

import android.text.InputFilter;
import android.text.Editable;

import android.text.Spanned;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import android.view.Menu;
import android.view.MenuItem;

import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class EditorActivity extends CommonActivity {
  private final static String LOG_TAG = EditorActivity.class.getName();

  private File filesDirectory;
  private SharedPreferences prefs;

  public ArrayList<String> recentURIs = new ArrayList<String>();
  private final static String PREF_RECENT_URIS = "recent-URIs";

  private final void restoreRecentURIs () {
    Set<String> uris = prefs.getStringSet(PREF_RECENT_URIS, null);
    if (uris != null) recentURIs.addAll(uris);
  }

  private final void saveRecentURI (ContentHandle handle) {
    String uri = handle.getNormalizedString();
    boolean resave = !recentURIs.remove(uri);

    while (recentURIs.size() >= ApplicationParameters.RECENT_URI_LIMIT) {
      recentURIs.remove(0);
      resave = true;
    }

    recentURIs.add(uri);

    if (resave) {
      prefs.edit()
           .putStringSet(PREF_RECENT_URIS, new LinkedHashSet<String>(recentURIs))
           .commit();
    }
  }

  private final static String PREF_CHECKPOINT_PREFIX = "checkpoint-";
  private final static String PREF_CHECKPOINT_NAME = PREF_CHECKPOINT_PREFIX + "name";
  private final static String PREF_CHECKPOINT_PATH = PREF_CHECKPOINT_PREFIX + "path";
  private final static String PREF_CHECKPOINT_TYPE = PREF_CHECKPOINT_PREFIX + "type";
  private final static String PREF_CHECKPOINT_WRITABLE = PREF_CHECKPOINT_PREFIX + "writable";
  private final static String PREF_CHECKPOINT_START = PREF_CHECKPOINT_PREFIX + "start";
  private final static String PREF_CHECKPOINT_END = PREF_CHECKPOINT_PREFIX + "end";
  private final static String PREF_CHECKPOINT_SPANS = PREF_CHECKPOINT_PREFIX + "spans";

  private final void removePreferenceKeys (SharedPreferences.Editor editor, String keyPrefix) {
    for (String key : prefs.getAll().keySet()) {
      if (key.startsWith(keyPrefix)) editor.remove(key);
    }
  }

  private EditArea editArea = null;
  private TextView uriView = null;

  public final EditArea getEditArea () {
    return editArea;
  }

  private final void showActivityResultCode (int code) {
  }

  private final boolean verifyTextRange (int start, int end) {
    return ApplicationUtilities.verifyTextRange(start, end, editArea.length());
  }

  private void setEditorContent (ContentHandle handle) {
    synchronized (this) {
      String path;

      if (handle != null) {
        path = handle.getNormalizedString();
      } else {
        path = getString(R.string.hint_new_file);
      }

      editArea.setContentHandle(handle);
      uriView.setText(path);
    }
  }

  private void setEditorContent (ContentHandle handle, CharSequence content) {
    synchronized (this) {
      setEditorContent(handle);
      editArea.setText(content);
      editArea.setHasChanged(false);
    }
  }

  public void setEditorContent () {
    setEditorContent(null, "");
  }

  public final boolean verifyWritableText () {
    if (!ApplicationSettings.PROTECT_TEXT) return true;
    showMessage(R.string.message_protected_text);
    return false;
  }

  public final boolean verifyWritableRegion (Spanned text, int start, int end) {
    if (verifyWritableText()) {
      if (!editArea.containsProtectedText(text, start, end)) return true;
      showMessage(R.string.message_protected_region);
    }

    return false;
  }

  public final boolean verifyWritableRegion (int start, int end) {
    return verifyWritableRegion(editArea.getText(), start, end);
  }

  public final boolean verifyWritableRegion () {
    return verifyWritableRegion(
      editArea.getSelectionStart(), editArea.getSelectionEnd()
    );
  }

  public final void performWithoutRegionProtection (Runnable operation) {
    boolean wasEnforced = editArea.getEnforceTextProtection();
    editArea.setEnforceTextProtection(false);

    try {
      operation.run();
    } finally {
      editArea.setEnforceTextProtection(wasEnforced);
    }
  }

  private final void saveFile (
    final File file, final CharSequence content,
    final boolean confirm, final Runnable onSaved
  ) {
    new AsyncTask<Void, Void, Void>() {
      AlertDialog dialog = null;

      @Override
      protected void onPreExecute () {
        if (confirm) {
          dialog = newAlertDialogBuilder(R.string.message_writing_content)
            .setMessage(file.getAbsolutePath())
            .create();

          dialog.show();
        }
      }

      @Override
      public Void doInBackground (Void... arguments) {
        Content.writeFile(file, content);
        return null;
      }

      @Override
      public void onPostExecute (Void result) {
        if (dialog != null) dialog.dismiss();

        if (confirm) {
          showMessage(R.string.message_file_saved, file.getAbsolutePath(), onSaved);
        } else {
          run(onSaved);
        }
      }
    }.execute();
  }

  private final void saveFile (File file, Runnable onSaved) {
    CharSequence content;

    synchronized (this) {
      if (file == null) {
        ContentHandle handle = editArea.getContentHandle();
        String detail;

        if (handle == null) {
          detail = ApplicationContext.getString(R.string.hint_new_file);
        } else if ((file = handle.getFile()) != null) {
          detail = null;
        } else {
          String name = handle.getProvidedName();

          if (name != null) {
            File directory = getDocumentsDirectory();

            if (directory != null) {
              file = new File(directory, name);
            }
          }

          detail = (file != null)? null: handle.getNormalizedString();
        }

        if (detail != null) {
          showMessage(R.string.message_save_no, detail);
          return;
        }
      }

      if (file.exists()) {
        int problem = 0;

        if (!file.isFile()) {
          problem = R.string.message_not_file;
        } else if (!file.canWrite()) {
          problem = R.string.message_not_writable;
        }

        if (problem != 0) {
          showMessage(problem, file.getAbsolutePath());
          return;
        }
      }

      content = editArea.getText();
      editArea.setHasChanged(false);
    }

    saveFile(file, content, true, onSaved);
  }

  private final void saveFile (Runnable onSaved) {
    saveFile(null, onSaved);
  }

  private final void saveFile (File file) {
    saveFile(file, null);
  }

  public final void saveFile () {
    saveFile(null, null);
  }

  public final void testHasChanged (final Runnable onSaved) {
    if (editArea.getHasChanged()) {
      OnDialogClickListener positiveListener = new OnDialogClickListener() {
        @Override
        public void onClick () {
          if (editArea.getContentHandle() != null) {
            saveFile(onSaved);
          } else {
            findFile(true, null,
              new FileFinder.FileHandler() {
                @Override
                public void handleFile (File file) {
                  if (file != null) saveFile(file, onSaved);
                }
              }
            );
          }
        }
      };

      OnDialogClickListener negativeListener = new OnDialogClickListener() {
        @Override
        public void onClick () {
          run(onSaved);
        }
      };

      newAlertDialogBuilder(R.string.changed_title)
        .setMessage(R.string.changed_message)
        .setPositiveButton(R.string.changed_button_positive, positiveListener)
        .setNeutralButton(R.string.changed_button_neutral, null)
        .setNegativeButton(R.string.changed_button_negative, negativeListener)
        .show();
    } else {
      run(onSaved);
    }
  }

  private final boolean loadContent (
    final ContentHandle handle, final Runnable onLoaded
  ) {
    new AsyncTask<Void, Void, CharSequence>() {
      AlertDialog dialog = null;

      @Override
      protected void onPreExecute () {
        dialog = newAlertDialogBuilder(R.string.message_reading_content)
          .setMessage(handle.getNormalizedString())
          .show();
      }

      @Override
      protected CharSequence doInBackground (Void... arguments) {
        try {
          final Editable input = new SpannableStringBuilder();
          Content.readContent(handle, input);
          return input.subSequence(0, input.length());
        } catch (OutOfMemoryError error) {
          System.gc();
          return null;
        }
      }

      @Override
      protected void onPostExecute (CharSequence content) {
        if (content != null) {
          final int maximum = 150000;
          int length = content.length();
          if (maximum < length) content = content.subSequence(0, maximum);

          setEditorContent(handle, content);
          run(onLoaded);
        }

        if (dialog != null) dialog.dismiss();
      }
    }.execute();

    return true;
  }

  public final boolean loadContent (ContentHandle handle) {
    saveRecentURI(handle);
    return loadContent(handle, null);
  }

  public final boolean loadContent (File file) {
    return loadContent(new ContentHandle(file));
  }

  private final File getDocumentsDirectory () {
    File directory = Environment.getExternalStoragePublicDirectory("Documents");

    if (!directory.exists()) {
      if (!directory.mkdir()) {
        Log.w(LOG_TAG, ("unable to create documents directory: " + directory.getAbsolutePath()));
        return null;
      }
    }

    return directory;
  }

  private final void addRootLocation (FileFinder.Builder builder, String name, File location) {
    builder.addRootLocation(FileSystems.makeLabel(name, location), location);
  }

  public final void addRootLocations (FileFinder.Builder builder) {
    {
      ContentHandle content = editArea.getContentHandle();

      if (content != null) {
        File file = content.getFile();

        if (file != null) {
          addRootLocation(builder, "current", file.getParentFile());
        }
      }
    }

    {
      File directory = getDocumentsDirectory();

      if (directory != null) {
        addRootLocation(builder, "documents", directory);
      }
    }

    for (String label : FileSystems.getRemovableLabels()) {
      builder.addRootLocation(label);
    }
  }

  public final FileFinder.Builder newFileFinderBuilder (boolean forWriting) {
    int title = forWriting?
                R.string.menu_file_SaveAs:
                R.string.menu_file_OpenFile;

    return new FileFinder
          .Builder(this)
          .setUserTitle(getString(title))
          .setForWriting(forWriting)
          ;
  }

  public final void findFile (
    boolean forWriting,
    String[] extensions,
    FileFinder.FileHandler handler
  ) {
    FileFinder.Builder builder = newFileFinderBuilder(forWriting);

    if (extensions != null) {
      for (String extension : extensions) {
        builder.addFileExtension(extension);
      }
    }

    {
      ContentHandle handle = editArea.getContentHandle();

      if (handle != null) {
        File file = handle.getFile();
        if (file != null) builder.setFileName(file.getName());
      }
    }

    addRootLocations(builder);
    builder.find(handler);
  }

  private final void saveAs (Content.FormatDescriptor format) {
    findFile(true,
      ((format != null)? format.getFileExtensions(): null),
      new FileFinder.FileHandler() {
        @Override
        public void handleFile (final File file) {
          if (file != null) {
            saveFile(file,
              new Runnable() {
                @Override
                public void run () {
                  ContentHandle handle = new ContentHandle(file);
                  setEditorContent(handle);
                  saveRecentURI(handle);
                }
              }
            );
          }
        }
      }
    );
  }

  private final void selectFormat () {
    final Content.FormatDescriptor[] formats = Content.getFormatDescriptors();
    String[] items = new String[1 + formats.length];

    {
      int count = 0;
      items[count++] = getString(R.string.format_select_all);

      for (Content.FormatDescriptor format : formats) {
        items[count++] = format.getSelectorLabel();
      }
    }

    DialogInterface.OnClickListener itemListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick (DialogInterface dialog, int index) {
        saveAs((index == 0)? null: formats[index-1]);
      }
    };

    newAlertDialogBuilder(R.string.format_request_select)
      .setItems(items, itemListener)
      .setNegativeButton(R.string.action_cancel, null)
      .show();
  }

  public final void confirmFormat () {
    ContentHandle handle = editArea.getContentHandle();
    File file = (handle != null)? handle.getFile(): null;

    if (file == null) {
      selectFormat();
    } else {
      String extension = Content.getFileExtension(file);

      final Content.FormatDescriptor format =
        (extension != null)?
        Content.getFormatDescriptorForFileExtension(extension):
        null;

      String message =
        (format != null)? format.getSelectorLabel():
        (extension == null)? getString(R.string.format_extension_none):
        String.format("%s (%s)",
          extension,
          getString(R.string.format_extension_unrecognized)
        );

      OnDialogClickListener okListener = new OnDialogClickListener() {
        @Override
        public void onClick () {
          saveAs(format);
        }
      };

      OnDialogClickListener changeListener = new OnDialogClickListener() {
        @Override
        public void onClick () {
          selectFormat();
        }
      };

      newAlertDialogBuilder(R.string.format_request_confirm)
        .setMessage(message)
        .setPositiveButton(R.string.action_ok, okListener)
        .setNeutralButton(R.string.action_change, changeListener)
        .setNegativeButton(R.string.action_cancel, null)
        .show();
    }
  }

  private final Map<Integer, EditorAction> editorActions =
        new HashMap<Integer, EditorAction>();

  private final EditorAction getEditorAction (int identifier) {
    EditorAction action = editorActions.get(identifier);
    if (action != null) return action;

    String name = getResources().getResourceEntryName(identifier);
    if (name == null) return null;

    name = name.replace('_', '.');
    name = getClass().getPackage().getName() + '.' + name;
    Class type;

    try {
      type = Class.forName(name);
    } catch (ClassNotFoundException exception) {
      return null;
    }

    if (!EditorAction.class.isAssignableFrom(type)) return null;
    Constructor constructor = LanguageUtilities.getConstructor(type, EditorActivity.class);
    if (constructor == null) return null;

    action = (EditorAction)LanguageUtilities.newInstance(constructor, this);
    editorActions.put(identifier, action);
    return action;
  }

  @Override
  public boolean onOptionsItemSelected (MenuItem item) {
    int identifier = item.getItemId();
    EditorAction action = getEditorAction(identifier);

    if (action != null) {
      action.performAction(item);
      return true;
    }

    String name = getResources().getResourceEntryName(identifier);
    if (name == null) name = Integer.toString(identifier);
    Log.w(LOG_TAG, ("unhandled menu action: " + name));

    return false;
  }

  @Override
  public boolean onCreateOptionsMenu (Menu menu) {
    getMenuInflater().inflate(R.menu.options, menu);
    MenuAction.setCurrentMenu(menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu (Menu menu) {
    MenuAction.setCurrentMenu(menu);
    return true;
  }

  private final void prepareMenuButton () {
    if (getActionBar() == null) {
      Button button = (Button)findViewById(R.id.menu_button);
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

  private final static void putPreference (
    SharedPreferences.Editor editor, String key, String value
  ) {
    if (value != null) {
      editor.putString(key, value);
    } else {
      editor.remove(key);
    }
  }

  public final void checkpointFile () {
    synchronized (this) {
      String oldName = prefs.getString(PREF_CHECKPOINT_NAME, null);
      final String newName;

      if (oldName == null) {
        newName = "checkpoint1";
      } else {
        StringBuilder sb = new StringBuilder(oldName);
        int last = oldName.length() - 1;

        sb.setCharAt(last, (oldName.charAt(last) == '1')? '2': '1');
        newName = sb.toString();
      }

      final File oldFile = (oldName != null)? new File(filesDirectory, oldName): null;
      final File newFile = new File(filesDirectory, newName);
      final ContentHandle handle = editArea.getContentHandle();

      final Editable content = new SpannableStringBuilder(editArea.getText());
      Markup.restoreRevisions(content);

      saveFile(newFile, content.toString(), false,
        new Runnable() {
          @Override
          public void run () {
            SharedPreferences.Editor editor = prefs.edit();
            removePreferenceKeys(editor, PREF_CHECKPOINT_PREFIX);
            editor.putString(PREF_CHECKPOINT_NAME, newFile.getName());

            if (handle != null) {
              putPreference(editor, PREF_CHECKPOINT_PATH, handle.getUriString());
              putPreference(editor, PREF_CHECKPOINT_TYPE, handle.getType());
              editor.putBoolean(PREF_CHECKPOINT_WRITABLE, handle.canWrite());
            }

            putPreference(
              editor, PREF_CHECKPOINT_SPANS, Spans.saveSpans(content)
            );

            editor.putInt(PREF_CHECKPOINT_START, editArea.getSelectionStart());
            editor.putInt(PREF_CHECKPOINT_END, editArea.getSelectionEnd());

            if (editor.commit()) {
              if (oldFile != null) {
                oldFile.delete();
              }
            }
          }
        }
      );
    }
  }

  private final void restoreFile () {
    synchronized (this) {
      String name = prefs.getString(PREF_CHECKPOINT_NAME, null);
      final String path = prefs.getString(PREF_CHECKPOINT_PATH, null);

      if (name != null) {
        loadContent(
          new ContentHandle(new File(filesDirectory, name), null, true),
          new Runnable() {
            @Override
            public void run () {
              int length = editArea.length();
              String type = prefs.getString(PREF_CHECKPOINT_TYPE, null);
              boolean writable = prefs.getBoolean(PREF_CHECKPOINT_WRITABLE, true);

              {
                ContentHandle handle;

                if ((path == null) || path.isEmpty()) {
                  handle = null;
                } else if (path.charAt(0) == File.separatorChar) {
                  handle = new ContentHandle(new File(path), type, writable);
                } else {
                  handle = new ContentHandle(path, type, writable);
                }

                setEditorContent(handle);
                if (handle != null) saveRecentURI(handle);
              }

              Spans.restoreSpans(
                editArea.getText(),
                prefs.getString(PREF_CHECKPOINT_SPANS, null)
              );

              {
                int start = prefs.getInt(PREF_CHECKPOINT_START, -1);
                int end = prefs.getInt(PREF_CHECKPOINT_END, -1);

                if (ApplicationUtilities.verifyTextRange(start, end, length)) {
                  editArea.setSelection(start, end);
                }
              }
            }
          }
        );
      }
    }
  }

  private final void editContent (ContentHandle handle) {
    loadContent(handle);
  }

  private final void addInputFilters () {
    editArea.setFilters(
      new InputFilter[] {
        new EditAreaFilter(this)
      }
    );
  }

  private final void setup () {
    ApplicationContext.setMainActivity(this);

    filesDirectory = getFilesDir();
    prefs = getSharedPreferences("editor", MODE_PRIVATE);

    Controls.restore();
    restoreRecentURIs();

    setContentView(R.layout.editor);
    uriView = (TextView)findViewById(R.id.current_uri);
    editArea = (EditArea)findViewById(R.id.edit_area);
    setEditorContent();

    showReportedErrors();
    prepareMenuButton();
    addInputFilters();
  }

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setup();

    int result = RESULT_CANCELED;
    Intent intent = getIntent();

    if (intent != null) {
      String action = intent.getAction();

      if (action != null) {
        Uri uri = intent.getData();
        String type = intent.getType();

        if (action.equals(Intent.ACTION_MAIN)) {
          restoreFile();
          result = RESULT_OK;
        } else if (action.equals(Intent.ACTION_VIEW)) {
          editContent(new ContentHandle(uri, type, false));
          result = RESULT_OK;
        } else if (action.equals(Intent.ACTION_EDIT)) {
          editContent(new ContentHandle(uri, type, true));
          result = RESULT_OK;
        }
      }
    }

    setResult(result);
  }

  @Override
  protected void onResume () {
    super.onResume();

    editArea.requestFocus();
  }

  @Override
  protected void onPause () {
    super.onPause();
    checkpointFile();
  }

  @Override
  protected void onDestroy () {
    try {
    } finally {
      super.onDestroy();
    }
  }
}
