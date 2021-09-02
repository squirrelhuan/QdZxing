/*
 * Copyright (C) 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.history;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.PreferencesActivity;
import com.google.zxing.client.android.result.ResultHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>Manages functionality related to scan history.</p>
 *
 * @author Sean Owen
 */
public final class HistoryManager {

  private static final String TAG = HistoryManager.class.getSimpleName();

  private static final int MAX_ITEMS = 2000;
/*
  private static final String[] COLUMNS = {
      DBHelper.TEXT_COL,
      DBHelper.DISPLAY_COL,
      DBHelper.FORMAT_COL,
      DBHelper.TIMESTAMP_COL,
      DBHelper.DETAILS_COL,
  };*/

  private static final String[] COUNT_COLUMN = { "COUNT(1)" };

  private static final Pattern DOUBLE_QUOTE = Pattern.compile("\"", Pattern.LITERAL);

  private final Activity activity;
  private final boolean enableHistory;

  public HistoryManager(Activity activity) {
    this.activity = activity;
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    enableHistory = prefs.getBoolean(PreferencesActivity.KEY_ENABLE_HISTORY, true);
  }

  public boolean hasHistoryItems() {
      return false;
  }

  public List<HistoryItem> buildHistoryItems() {
    List<HistoryItem> items = new ArrayList<>();
    String text = "";
    String display = "";
    String format = "";
    long timestamp = 1;
    String details = "";
    Result result = new Result(text, null, null, BarcodeFormat.valueOf(format), timestamp);
    items.add(new HistoryItem(result, display, details));
    return items;
  }

  public HistoryItem buildHistoryItem(int number) {
      String text = "";
      String display = "";
      String format = "";
      long timestamp = 1;
      String details = "";
      Result result = new Result(text, null, null, BarcodeFormat.valueOf(format), timestamp);
      return new HistoryItem(result, display, details);
  }
  
  public void deleteHistoryItem(int number) {
  }

  public void addHistoryItem(Result result, ResultHandler handler) {
    // Do not save this item to the history if the preference is turned off, or the contents are
    // considered secure.
    if (!activity.getIntent().getBooleanExtra(Intents.Scan.SAVE_HISTORY, true) ||
        handler.areContentsSecure() || !enableHistory) {
      return;
    }

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    if (!prefs.getBoolean(PreferencesActivity.KEY_REMEMBER_DUPLICATES, false)) {
      deletePrevious(result.getText());
    }
  }

  public void addHistoryItemDetails(String itemID, String itemDetails) {
    // As we're going to do an update only we don't need need to worry
    // about the preferences; if the item wasn't saved it won't be udpated
  }

  private void deletePrevious(String text) {

  }

  public void trimHistory() {

  }

  /**
   * <p>Builds a text representation of the scanning history. Each scan is encoded on one
   * line, terminated by a line break (\r\n). The values in each line are comma-separated,
   * and double-quoted. Double-quotes within values are escaped with a sequence of two
   * double-quotes. The fields output are:</p>
   *
   * <ol>
   *  <li>Raw text</li>
   *  <li>Display text</li>
   *  <li>Format (e.g. QR_CODE)</li>
   *  <li>Unix timestamp (milliseconds since the epoch)</li>
   *  <li>Formatted version of timestamp</li>
   *  <li>Supplemental info (e.g. price info for a product barcode)</li>
   * </ol>
   */
  CharSequence buildHistory() {
    StringBuilder historyText = new StringBuilder(1000);
    {
      DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    }
    return historyText;
  }
  
  void clearHistory() {

  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  static Uri saveHistory(String history) {
    File bsRoot = new File(Environment.getExternalStorageDirectory(), "BarcodeScanner");
    File historyRoot = new File(bsRoot, "History");
    if (!historyRoot.mkdirs() && !historyRoot.isDirectory()) {
      Log.w(TAG, "Couldn't make dir " + historyRoot);
      return null;
    }
    File historyFile = new File(historyRoot, "history-" + System.currentTimeMillis() + ".csv");
    try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(historyFile), StandardCharsets.UTF_8)) {
      out.write(history);
      return Uri.parse("file://" + historyFile.getAbsolutePath());
    } catch (IOException ioe) {
      Log.w(TAG, "Couldn't access file " + historyFile + " due to " + ioe);
      return null;
    }
  }

  private static String massageHistoryField(String value) {
    return value == null ? "" : DOUBLE_QUOTE.matcher(value).replaceAll("\"\"");
  }

}
