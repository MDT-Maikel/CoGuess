/*
 * Copyright © 2019 Maikel de Vries
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.mdtmaikel.coguess;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class WordSetActivity extends AppCompatActivity
{
    private RecyclerView recycler_view;
    private WordSetRecyclerViewAdapter m_adapter;
    private RecyclerView.LayoutManager layout_manager;

    private String current_word_list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordset);

        // Change action bar.
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.custom_action_bar_wordset);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Retrieve current word list.
        ArrayList<String> word_lists = MainActivity.getInstance().getAllWordLists();
        if (!word_lists.isEmpty())
        {
            Collections.sort(word_lists, String.CASE_INSENSITIVE_ORDER);
            current_word_list = word_lists.get(0);
        }
        SetCurrentWordListTitle();

        // Init edit text field.
        initEditTextField();

        // Init word list checkbox.
        initWordListCheckbox();

        // Create recycler view.
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view_wordset);
        layout_manager = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(layout_manager);

        // Update recycler list.
        updateListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Enables icons in the options menu.
        if (menu instanceof MenuBuilder)
        {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_wordset, menu);
        // Fix the text color.
        for (int i = 0; i < menu.size(); i++)
        {
            MenuItem item = menu.getItem(i);
            SpannableString span_string = new SpannableString(menu.getItem(i).getTitle().toString());
            span_string.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAppText)),0, span_string.length(),0);
            item.setTitle(span_string);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.opt_wordset_new_list:
                // Open dialog to enter name using alert dialog and edit text.
                AlertDialog.Builder builder_new_list = new AlertDialog.Builder(this);
                builder_new_list.setCancelable(true);
                builder_new_list.setTitle(R.string.options_wordset_new_list_title);
                builder_new_list.setMessage(R.string.options_wordset_new_list_input);
                // Put the edit text in a linear layout to fit the alert dialog better.
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                input.setKeyListener(TextKeyListener.getInstance(/*"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZäëïöüÄËÏÖÜß-"*/));
                input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
                input.setSingleLine(true);
                input.setHint(R.string.wordset_wordset_new_list_hint);
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams layout_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                int side_margin = 60;
                layout_params.setMargins(side_margin, 0, side_margin, 0);
                layout.addView(input, layout_params);
                builder_new_list.setView(layout);
                // Retrieve list name on confirmation.
                builder_new_list.setPositiveButton(R.string.delete_confirm,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                String word_list_name = input.getText().toString();
                                SpannedString msg = SpanFormatter.format(getString(R.string.options_wordset_new_list_not_created));
                                if (MainActivity.getInstance().addCustomWordList(word_list_name))
                                {
                                    String language = AppUtility.LanguageIDtoString(MainActivity.getInstance().getSettingsLanguage());
                                    msg = SpanFormatter.format(getString(R.string.options_wordset_new_list_created), word_list_name, language);
                                }
                                current_word_list = word_list_name;
                                // Update title and word list.
                                SetCurrentWordListTitle();
                                updateListView();
                                updateWordListCheckbox();
                                // Notify user through a toast message.
                                AppUtility.displayToastLong(getApplicationContext(), msg);
                            }
                        });
                builder_new_list.setNegativeButton(R.string.delete_cancel,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        });
                builder_new_list.show();
                return true;

            case R.id.opt_wordset_delete_list:
                // Can not delete list if there is no existing word list.
                if (current_word_list == null)
                {
                    AppUtility.displayToastShort(getApplicationContext(), R.string.options_wordset_delete_toast_failed);
                    return true;
                }

                // Delete list after asking for confirmation.
                AlertDialog.Builder builder_delete_list = new AlertDialog.Builder(this);
                builder_delete_list.setCancelable(true);
                builder_delete_list.setTitle(R.string.options_wordset_delete_list);
                builder_delete_list.setMessage(R.string.options_wordset_delete_confirmation);
                builder_delete_list.setPositiveButton(R.string.delete_confirm,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // Proceed with deletion.
                                deleteWordList();
                            }
                        });
                builder_delete_list.setNegativeButton(R.string.delete_cancel,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // No need to do anything.
                            }
                        });
                builder_delete_list.show();
                return true;

            case R.id.opt_wordset_copy:
                // Can not copy if there is no existing word list.
                if (current_word_list == null)
                {
                    AppUtility.displayToastShort(getApplicationContext(), R.string.options_wordset_copy_toast_failed);
                    return true;
                }

                // Get custom word list.
                ArrayList<String> wordset_list = (ArrayList<String>) MainActivity.getInstance().getCustomWordSet(current_word_list);

                // Copy all words of this list to the clipboard.
                ClipboardManager clipboard_copy = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("copied_wordset", getCustomWordListAsString(wordset_list));
                clipboard_copy.setPrimaryClip(clip);

                // Show message that list has been copied to clipboard.
                AppUtility.displayToastLong(getApplicationContext(), String.format(getResources().getString(R.string.options_wordset_copy_toast), wordset_list.size()));
                return true;

            case R.id.opt_wordset_paste:
                // Can not paste if there is no existing word list.
                if (current_word_list == null)
                {
                    AppUtility.displayToastShort(getApplicationContext(), R.string.options_wordset_paste_toast_failed);
                    return true;
                }

                // Get word list from clipboard
                String word_list = null;
                ClipboardManager clipboard_paste = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard_paste != null && clipboard_paste.hasPrimaryClip())
                    word_list = clipboard_paste.getPrimaryClip().getItemAt(0).getText().toString();

                // Insert words into the custom database.
                int[] result = importCustomWordListFromString(word_list);

                // Show message that list has been pasted from clipboard.
                AppUtility.displayToastLong(getApplicationContext(), String.format(getResources().getString(R.string.options_wordset_paste_toast), result[0], result[1]));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getCustomWordListAsString(ArrayList<String> wordset_list)
    {
        // Reverse list so that last added word appears first.
        Collections.reverse(wordset_list);
        // Construct word set string.
        String wordset_string = "";
        for(String word : wordset_list)
        {
            wordset_string = wordset_string + word + "\n";
        }
        return wordset_string;
    }

    public int[] importCustomWordListFromString(String word_list)
    {
        // Assume words are separated by line breaks.
        String[] words = word_list.split("\n");
        ArrayList<String> wordset_list = new ArrayList<String>(Arrays.asList(words));
        Collections.reverse(wordset_list);
        int[] result = new int[] {0, wordset_list.size()};
        for (String word : wordset_list)
        {
            if (MainActivity.getInstance().addCustomWord(word, current_word_list))
                result[0]++;
        }
        // Update word list.
        updateListView();
        // Return how many of how many words were added.
        return result;
    }

    public void updateListView()
    {
        // Get custom word set.
        ArrayList<String> wordset_list = new ArrayList<String>();
        if (current_word_list != null)
             wordset_list = MainActivity.getInstance().getCustomWordSet(current_word_list);

        // Reverse list so that last added word appears first.
        Collections.reverse(wordset_list);

        // Transfer words to recycler view.
        m_adapter = new WordSetRecyclerViewAdapter(wordset_list);
        recycler_view.setAdapter(m_adapter);
        setOnItemListener();
    }

    private void initEditTextField()
    {
        // Get edit text field.
        EditText edit_text = (EditText) findViewById(R.id.enter_wordset_word);
        // Add editor action listener to store word on finishing edit text by user.
        edit_text.setOnEditorActionListener(
                new EditText.OnEditorActionListener()
                {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                    {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        {
                            if (event == null || !event.isShiftPressed())
                            {
                                // Get the entered word.
                                EditText edit_text = findViewById(R.id.enter_wordset_word);
                                String word = edit_text.getText().toString();

                                // Reset text entry field.
                                edit_text.setText(null);

                                // Check current word list.
                                if (current_word_list == null)
                                {
                                    // Show message that there is no list the word can be added to.
                                    AppUtility.displayToastShort(getApplicationContext(), R.string.wordset_word_no_list_available);
                                    return true;
                                }

                                // Try to store word.
                                if (word.isEmpty() || !MainActivity.getInstance().addCustomWord(word, current_word_list))
                                {
                                    // Show message that word is already in list.
                                    AppUtility.displayToastShort(getApplicationContext(), R.string.wordset_word_already_in_list);
                                    return true;
                                }

                                // Show message that word has been added, but check if in any of the main databases for message.
                                if (MainActivity.getInstance().isExistingMainEntry(word))
                                    AppUtility.displayToastLong(getApplicationContext(), String.format(getResources().getString(R.string.wordset_word_added_exists_in_main), word));
                                else
                                    AppUtility.displayToastShort(getApplicationContext(), String.format(getResources().getString(R.string.wordset_word_added), word));

                                // Update word list.
                                updateListView();
                                return true;
                            }
                        }
                        return false;
                    }
                }
        );
        // Add an edit text watcher to check if word already exists in other databases.
        edit_text.addTextChangedListener(
                new TextWatcher()
                {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                    @Override
                    public void afterTextChanged(Editable s)
                    {
                        if (current_word_list == null)
                            return;

                        String entered_text = s.toString();
                        if (MainActivity.getInstance().isExistingCustomEntry(entered_text, current_word_list))
                        {
                            // Custom entry already exists in this word list so never add and turn color to red.
                            edit_text.setTextColor(getColor(R.color.colorAppRed));
                        }
                        else if (MainActivity.getInstance().isExistingMainEntry(entered_text))
                        {
                            // Entry already exists in one of the shipped databases so turn color to orange.
                            edit_text.setTextColor(getColor(R.color.colorAppOrange));
                        }
                        else
                        {
                            // Change back to default text entry color.
                            edit_text.setTextColor(Color.BLACK);
                        }
                    }
                }
        );
    }

    public void setOnItemListener()
    {
        if (m_adapter != null)
        {
            m_adapter.setOnItemClick(new WordSetRecyclerViewAdapter.OnItemClickListener()
            {
                @Override
                public void onItemClick(View view, String word)
                {
                    // Delete clicked word.
                    MainActivity.getInstance().deleteCustomWord(word, current_word_list);
                    updateListView();
                }
            });
        }
    }


    /*-- Word List Methods --*/

    public void buttonOnSelectPreviousClick(View v)
    {
        ArrayList<String> word_lists = MainActivity.getInstance().getAllWordLists();
        if (!word_lists.isEmpty())
        {
            int index = word_lists.indexOf(current_word_list);
            index = Math.floorMod(index - 1, word_lists.size());
            current_word_list = word_lists.get(index);
            // Update title and word list.
            SetCurrentWordListTitle();
            updateListView();
            updateWordListCheckbox();
        }
    }

    public void buttonOnSelectNextClick(View v)
    {
        ArrayList<String> word_lists = MainActivity.getInstance().getAllWordLists();
        if (!word_lists.isEmpty())
        {
            int index = word_lists.indexOf(current_word_list);
            index = Math.floorMod(index + 1, word_lists.size());
            current_word_list = word_lists.get(index);
            // Update title and word list.
            SetCurrentWordListTitle();
            updateListView();
            updateWordListCheckbox();
        }
    }

    public void SetCurrentWordListTitle()
    {
        String language = MainActivity.getInstance().getSettingsLanguage();
        String title_text = getString(R.string.wordset_no_list);
        if (current_word_list != null)
            title_text = current_word_list + " (" + language.toUpperCase() + ")";
        ((TextView) findViewById(R.id.wordlist_current)).setText(title_text);
    }


    private void deleteWordList()
    {
        // Delete all words in the current list.
        if (current_word_list != null)
            MainActivity.getInstance().deleteAllCustomWords(current_word_list);
        // Select another word list.
        ArrayList<String> word_lists = MainActivity.getInstance().getAllWordLists();
        if (!word_lists.isEmpty())
        {
            Collections.sort(word_lists, String.CASE_INSENSITIVE_ORDER);
            current_word_list = word_lists.get(0);
        }
        else
        {
            current_word_list = null;
        }
        // Update title and word list.
        SetCurrentWordListTitle();
        updateListView();
        updateWordListCheckbox();
    }

    public void initWordListCheckbox()
    {
        CheckBox checkbox = (CheckBox) findViewById(R.id.wordlist_checkbox_active);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton compound_button, boolean is_checked)
                {
                    // Only do stuff for user-press events.
                    // TODO: This breaks accessibility mode! isPressed() is not true when it is triggered by a double tap from voice assistant mode.
                    if (!compound_button.isPressed())
                        return;

                    if (is_checked)
                    {
                        // Make current word list active.
                        MainActivity.getInstance().setWordListActive(current_word_list, true);
                        // Show message that word is already in list.
                        AppUtility.displayToastLong(getApplicationContext(), String.format(getString(R.string.wordset_list_activated), current_word_list));
                    }
                    else
                    {
                        // Make current word list inactive.
                        MainActivity.getInstance().setWordListActive(current_word_list, false);
                        // Show message that word is already in list.
                        AppUtility.displayToastLong(getApplicationContext(), String.format(getString(R.string.wordset_list_deactivated), current_word_list));
                    }
                }
            }
        );
        // Update checkbox.
        updateWordListCheckbox();
    }

    public void updateWordListCheckbox()
    {
        CheckBox checkbox = (CheckBox) findViewById(R.id.wordlist_checkbox_active);
        if (current_word_list == null)
        {
            checkbox.setVisibility(View.INVISIBLE);
        }
        else
        {
            checkbox.setVisibility(View.VISIBLE);
            // Find whether current word list is active.
            if (MainActivity.getInstance().getWordListActive(current_word_list))
                checkbox.setChecked(true);
            else
                checkbox.setChecked(false);
        }
    }
}