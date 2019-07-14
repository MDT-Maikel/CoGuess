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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.method.DigitsKeyListener;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class WordSetActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private WordSetRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

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

        // Set text according to language.
        SharedPreferences sharedConfig = PreferenceManager.getDefaultSharedPreferences(this);
        String language = AppUtility.LanguageIDtoString(sharedConfig.getString("list_languages", "en"));
        ((TextView) findViewById(R.id.text_about)).setText(SpanFormatter.format(getText(R.string.wordset_text), language));

        // Init edit text field.
        initEditTextField();

        // Create recycler view.
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_wordset);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

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
                // Open dialog to enter name.
                AlertDialog.Builder builder_new_list = new AlertDialog.Builder(this);
                builder_new_list.setCancelable(true);
                builder_new_list.setTitle(R.string.options_wordset_new_list_title);
                builder_new_list.setMessage(R.string.options_wordset_new_list_input);
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                input.setKeyListener(DigitsKeyListener.getInstance("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZäëïöüÄËÏÖÜß-"));
                input.setSingleLine(true);
                input.setHint(R.string.wordset_wordset_new_list_hint);
                builder_new_list.setView(input);
                builder_new_list.setPositiveButton(R.string.delete_confirm,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                String text = input.getText().toString();
                                // TODO: make the new custom list.
                                Toast toast_copy = Toast.makeText(getApplicationContext(), R.string.options_wordset_new_list_created, Toast.LENGTH_SHORT);
                                toast_copy.setGravity(Gravity.CENTER, 0, 0);
                                toast_copy.show();;
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
                // Get custom word list.
                ArrayList<String> wordset_list = (ArrayList<String>) MainActivity.getInstance().getCustomWordSet();

                // Copy all words of this list to the clipboard.
                ClipboardManager clipboard_copy = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("copied_wordset", getCustomWordListAsString(wordset_list));
                clipboard_copy.setPrimaryClip(clip);

                // Show message that list has been copied to clipboard.
                Toast toast_copy = Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.options_wordset_copy_toast), wordset_list.size()), Toast.LENGTH_LONG);
                toast_copy.setGravity(Gravity.CENTER, 0, 0);
                toast_copy.show();
                return true;

            case R.id.opt_wordset_paste:
                // Get word list from clipboard
                String word_list = null;
                ClipboardManager clipboard_paste = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard_paste != null && clipboard_paste.hasPrimaryClip())
                    word_list = clipboard_paste.getPrimaryClip().getItemAt(0).getText().toString();

                // Insert words into the custom database.
                int[] result = importCustomWordListFromString(word_list);

                // Show message that list has been pasted from clipboard.
                Toast toast_paste = Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.options_wordset_paste_toast), result[0], result[1]), Toast.LENGTH_LONG);
                toast_paste.setGravity(Gravity.CENTER, 0, 0);
                toast_paste.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteWordList()
    {
        // Delete all words in this list.
        MainActivity.getInstance().deleteAllCustomWords();
        // Update word list.
        updateListView();
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
            String current_word_list = "";
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
        ArrayList<String> wordset_list = (ArrayList<String>) MainActivity.getInstance().getCustomWordSet();
        // Reverse list so that last added word appears first.
        Collections.reverse(wordset_list);

        // Transfer words to recycler view.
        mAdapter = new WordSetRecyclerViewAdapter(wordset_list);
        recyclerView.setAdapter(mAdapter);
        setOnItemListener();
    }

    private void initEditTextField()
    {
        // Add edit text listener to store word.
        EditText edit_text = (EditText) findViewById(R.id.enter_wordset_word);
        edit_text.setOnEditorActionListener(
                new EditText.OnEditorActionListener()
                {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                    {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || (event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        {
                            if (event == null || !event.isShiftPressed())
                            {
                                // TODO: Check if word already exists.

                                // Show message that word has been added.
                                Toast toast = Toast.makeText(getApplicationContext(), R.string.wordset_word_added, Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                // Store and reset word.
                                EditText edit_text = findViewById(R.id.enter_wordset_word);
                                String word = edit_text.getText().toString();
                                String current_word_list = "";
                                MainActivity.getInstance().addCustomWord(word, current_word_list);
                                edit_text.setText(null);

                                // Update word list.
                                updateListView();
                                return true;
                            }
                        }
                        return false;
                    }
                }
        );
    }

    public void setOnItemListener()
    {
        if (mAdapter != null)
        {
            mAdapter.setOnItemClick(new WordSetRecyclerViewAdapter.OnItemClickListener()
            {
                @Override
                public void onItemClick(View view, String word)
                {
                    // Delete clicked word.
                    MainActivity.getInstance().deleteCustomWord(word);
                    updateListView();
                }
            });
        }
    }
}