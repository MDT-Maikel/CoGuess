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


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;


public class MainActivity extends AppCompatActivity
{
	/* Class Variables */
	private int score;
	private int word_counter;
	private ArrayList<String> word_list = new ArrayList<String>();
	private String current_word;
	private String word_state; // "reveal", "guessing"
	private String word_vis; // "visible", invisible"
	private String game_state; // "ready", "running"

	private CountDownTimerExt countdown_timer;
	private long countdown_time_remaining;
	private CountDownTimer word_fade_timer;

	private DBWordSet db_wordset;
	private DBCustomWordSet db_custom_wordset;
	private DBScore db_score;


	/* Class Methods */

	private static MainActivity instance;

	public static MainActivity getInstance()
	{
		return instance;
	}

	public static Context getContext()
	{
		return instance;
	}


	/* Button Click Events */

	public void buttonOnWordClick(View v)
	{
		if (game_state == "running")
		{
			if (word_state == "reveal")
			{
				setWordButton(v);
				fadeWordButton();
			}
			else if (word_state == "guessing" && word_vis == "invisible")
			{
				word_vis = "visible";
				Button b = findViewById(R.id.button_word);
				b.setText(current_word);
				fadeWordButton();
			}
		}
	}

	public void buttonOnTimerClick(View v)
	{
		if (game_state == "ready")
		{
			// Initialize word list to current settings and check if it works.
			initWordList();
			if (word_list.isEmpty())
			{
				Toast toast = Toast.makeText(getApplicationContext(), R.string.no_word_set_selected, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;
			}
			else
			{
				Toast toast = Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.display_word_set_size), word_list.size()), Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}


			// Get duration from settings and start countdown.
			SharedPreferences sharedConfig = PreferenceManager.getDefaultSharedPreferences(this);
			String duration = sharedConfig.getString("list_durations", "15");

			int countdown_duration = Integer.parseInt(duration) * 60 * 1000;
			countdown_time_remaining = countdown_duration;
			countdown_timer = new CountDownTimerExt(countdown_duration, 1000);
			countdown_timer.start();

			// Update internals and interface.
			game_state = "running";
			score = 0;
			setPrimaryButtonViewState(View.VISIBLE);
			updateScore();
		}
	}

	public void buttonOnCorrectClick(View v)
	{
		if (word_state == "guessing")
		{
			score++;
			updateScore();
			resetWordButton();
			playSound(R.raw.cleared);
		}
	}

	public void buttonOnWrongClick(View v)
	{
		if (word_state == "guessing")
		{
			resetWordButton();
			playSound(R.raw.error);

			// Reduce game duration by amount in preferences.
			SharedPreferences sharedConfig = PreferenceManager.getDefaultSharedPreferences(this);
			String penalty = sharedConfig.getString("list_penalties", "0");
			int time_penalty = Integer.parseInt(penalty);
			if (time_penalty > 0)
			{
				countdown_time_remaining = countdown_timer.getTimeRemaining();
				countdown_timer.cancel();
				countdown_timer = new CountDownTimerExt(Math.max(countdown_time_remaining - time_penalty * 1000, 1000), 1000);
				countdown_timer.start();
			}
		}
	}

	public void buttonOnDictClick(View v)
	{
		String language = getSettingsLanguage();
		if (language.equals("en") || language.equals("de"))
			openURL("https://dict.leo.org/german-english/" + current_word);
		else if (language.equals("nl"))
			openURL("https://www.woorden.org/woord/" + current_word);
		else
			/* EMPTY */;
	}

	public void buttonOnWikiClick(View v)
	{
		String language = getSettingsLanguage();
		openURL("https://" + language + ".wikipedia.org/wiki/" + current_word);
	}

	public void buttonOnGoogleClick(View v)
	{
		String language = getSettingsLanguage();
		if (language.equals("en"))
			language = "com";
		openURL("https://www.google." + language + "/search?tbm=isch&q=" + current_word);
	}

	private void openURL(String url)
	{
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	private void setWordButton(View v)
	{
		current_word = getNextWord();
		word_state = "guessing";
		word_vis = "visible";

		Button b = findViewById(R.id.button_word);
		b.setText(current_word);
		setSecondaryButtonViewState(View.VISIBLE);
	}

	private void resetWordButton()
	{
		current_word = null;
		word_state = "reveal";
		word_vis = "invisible";

		Button b = findViewById(R.id.button_word);
		b.setText(R.string.button_reveal);
		setSecondaryButtonViewState(View.INVISIBLE);
	}

	private void fadeWordButton()
	{
		// Hide button after a few seconds and add option to show the word.
		if (word_fade_timer != null)
			word_fade_timer.cancel();
		word_fade_timer = new CountDownTimer(3000, 1000)
		{
			public void onTick(long millisUntilFinished) {}
			public void onFinish()
			{
				if (word_state == "guessing")
				{
					Button b = findViewById(R.id.button_word);
					b.setText(R.string.button_show);
					word_vis = "invisible";
				}
			}
		}.start();
	}

	private void setPrimaryButtonViewState(int state)
	{
		Button bw = findViewById(R.id.button_word);
		bw.setVisibility(state);
		TextView ts = findViewById(R.id.text_score);
		ts.setVisibility(state);
	}

	private void setSecondaryButtonViewState(int state)
	{
		ImageButton bc = findViewById(R.id.image_button_correct);
		bc.setVisibility(state);
		ImageButton bw = findViewById(R.id.image_button_wrong);
		bw.setVisibility(state);
		ImageButton bd = findViewById(R.id.image_button_dict);
		bd.setVisibility(state);
		ImageButton bk = findViewById(R.id.image_button_wiki);
		bk.setVisibility(state);
		ImageButton bg = findViewById(R.id.image_button_img);
		bg.setVisibility(state);
	}

	public void resetGameState()
	{
		game_state = "ready";
		setPrimaryButtonViewState(View.INVISIBLE);
		setSecondaryButtonViewState(View.INVISIBLE);
		resetWordButton();
	}

	private void playSound(int sound)
	{
		final MediaPlayer mp = MediaPlayer.create(MainActivity.getContext(), sound);
		mp.start();
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			public void onCompletion(MediaPlayer player)
			{
				player.reset();
				player.release();
			}
		});
	}


	/* Word Methods */

	private void initWordList()
	{
		readWordsFromDatabase();
		word_counter = 0;
	}

	private void readWordsFromDatabase()
	{
		// Determine game language.
		String language = getSettingsLanguage();
		// Determine word set difficulty and type.
		//Set<String> word_set_difficulties = getSettingsWordDifficulties();
		Set<String> word_set_categories = getSettingsWordCategories();
		if (/*word_set_difficulties == null || */word_set_categories == null)
			return;
		int word_difficulties = 0;
		int word_categories = 0;
		//Iterator iter_difficulties = word_set_difficulties.iterator();
		//while (iter_difficulties.hasNext())
		//	word_difficulties += Integer.parseInt((String) iter_difficulties.next());
		Iterator iter_types = word_set_categories.iterator();
		while (iter_types.hasNext())
			word_categories += Integer.parseInt((String) iter_types.next());

		// Get the specified words (difficulty and type).
		word_list = db_wordset.getAllWords(language, Constants.WORDSET_DIFFICULTY_ALL/*word_difficulties*/, word_categories);

		// Add custom word lists activated by user.
		word_list.addAll(db_custom_wordset.getAllActivatedWords(language));

		// Shuffle words.
		Collections.shuffle(word_list);
	}

	private String getNextWord()
	{
		// Get next word from the shuffled word list.
		String word = word_list.get(word_counter);
		word_counter++;
		word_counter = word_counter % word_list.size();
		return word;
	}

	public boolean addCustomWord(String word, String for_word_list)
	{
		// Use currently selected language.
		String language = getSettingsLanguage();
		return db_custom_wordset.insertWord(word, language, for_word_list);
	}

	public void deleteCustomWord(String word, String for_word_list)
	{
		// Use currently selected language.
		String language = getSettingsLanguage();
		deleteCustomWord(word, language, for_word_list);
	}

	public void deleteCustomWord(String word, String language, String for_word_list)
	{
		db_custom_wordset.deleteWordByName(word, language, for_word_list);
	}

	public void deleteAllCustomWords(String for_word_list)
	{
		// Use currently selected language.
		String language = getSettingsLanguage();
		db_custom_wordset.deleteAllWords(language, for_word_list);
	}

	public ArrayList<String> getCustomWordSet(String for_word_list)
	{
		// Determine game language and get corresponding custom word set.
		String language = getSettingsLanguage();
		return getCustomWordSet(language, for_word_list);
	}

	public ArrayList<String> getCustomWordSet(String language, String for_word_list)
	{
		return db_custom_wordset.getAllWords(language, for_word_list);
	}

	public DBWordSet getWordSetDatabase()
	{
		return db_wordset;
	}

	public boolean addCustomWordList(String word_list_name)
	{
		// Use currently selected language.
		String language = getSettingsLanguage();
		return db_custom_wordset.addWordList(word_list_name, language);
	}

	public ArrayList<String> getAllWordLists()
	{
		// Determine game language and get corresponding custom word set lists.
		return db_custom_wordset.getAllWordLists(getSettingsLanguage());
	}

	public void setWordListActive(String word_list_name, boolean active)
	{
		db_custom_wordset.setWordListActive(word_list_name, getSettingsLanguage(), active);
	}

	public boolean getWordListActive(String word_list_name)
	{
		return db_custom_wordset.getWordListActive(word_list_name, getSettingsLanguage());
	}


	/* Settings */

	// Returns the game language.
	public String getSettingsLanguage()
	{
		SharedPreferences sharedConfig = PreferenceManager.getDefaultSharedPreferences(this);
		return sharedConfig.getString("list_languages", "en");
	}

	// Returns the selected word difficulties.
	public Set<String> getSettingsWordDifficulties()
	{
		SharedPreferences sharedConfig = PreferenceManager.getDefaultSharedPreferences(this);
		return sharedConfig.getStringSet("list_word_difficulties", null);
	}

	// Returns the selected word categories.
	public Set<String> getSettingsWordCategories()
	{
		SharedPreferences sharedConfig = PreferenceManager.getDefaultSharedPreferences(this);
		return sharedConfig.getStringSet("list_word_categories", null);
	}

	// Returns the game language.
	public String getSettingsTeamName()
	{
		SharedPreferences sharedConfig = PreferenceManager.getDefaultSharedPreferences(this);
		return sharedConfig.getString("text_team_name", getString(R.string.string_pref_team_name_default));
	}


	/* Score Keeping */

	private void updateScore()
	{
		TextView tv = (TextView) findViewById(R.id.text_score);
		tv.setText(String.format("%d", score));
	}

	private void storeScore()
	{
		// Show amount of points scored.
		Toast toast = Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.display_achieved_score), score), Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

		// Get game team name.
		String team_name = getSettingsTeamName();

		// Insert score into database.
		db_score.insertScore(team_name, score,"en", 30, "basic");
	}

	public ArrayList<Hashtable> getSortedScores()
	{
		ArrayList<Hashtable> scores = db_score.getAllSortedScores();
		return scores;
	}

	public void deleteAllScores()
	{
		db_score.deleteAllScores();
	}


	/* Highlighting */

	private void handleButtonHighlighting()
	{
		ImageButton button_dict = (ImageButton) findViewById(R.id.image_button_dict);
		button_dict.setOnTouchListener(new ButtonHighlighter(button_dict));
		ImageButton button_wiki = (ImageButton) findViewById(R.id.image_button_wiki);
		button_wiki.setOnTouchListener(new ButtonHighlighter(button_wiki));
		ImageButton button_img = (ImageButton) findViewById(R.id.image_button_img);
		button_img.setOnTouchListener(new ButtonHighlighter(button_img));

		ImageButton button_wrong = (ImageButton) findViewById(R.id.image_button_wrong);
		button_wrong.setOnTouchListener(new ButtonHighlighter(button_wrong));
		ImageButton button_correct = (ImageButton) findViewById(R.id.image_button_correct);
		button_correct.setOnTouchListener(new ButtonHighlighter(button_correct));
		// TODO: Can we have a ripple circle effect on these buttons somehow instead of this?
	}


	/* Event Handling */

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		instance = this;

		score = 0;
		word_state = "reveal";
		word_vis = "invisible";
		game_state = "ready";

		// Init word set and score databases.
		db_wordset = new DBWordSet(this);
		try
		{
			db_wordset.updateDataBase();
		}
		catch (IOException mIOException)
		{
			throw new Error("UnableToUpdateDatabase");
		}
		// TODO: Ensure reloading this database on every app start.
		db_custom_wordset = new DBCustomWordSet(this);
		db_score = new DBScore(this);

		// Screen stays on during play.
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Graphical effects.
		handleButtonHighlighting();

		// Change action bar.
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setCustomView(R.layout.custom_action_bar);
		}

		// Make buttons invisible on app start.
		setPrimaryButtonViewState(View.INVISIBLE);
		setSecondaryButtonViewState(View.INVISIBLE);

		// Init preferences.
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (countdown_timer != null)
		{
			countdown_time_remaining = countdown_timer.getTimeRemaining();
			countdown_timer.cancel();
			countdown_timer = null;
		}

	}

	@Override
	protected void onResume()
	{
		super.onResume();

		System.out.println(game_state);
		System.out.println(countdown_timer);
		if (countdown_timer == null && game_state == "running")
		{
			countdown_timer = new CountDownTimerExt(countdown_time_remaining, 1000);
			countdown_timer.start();
		}
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
		inflater.inflate(R.menu.options_main, menu);
		// Fix the text color.
		for(int i = 0; i < menu.size(); i++) {
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
			case R.id.opt_settings:
				Intent intent_settings = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent_settings);
				return true;
			case R.id.opt_high_scores:
				Intent intent_scores = new Intent(MainActivity.this, ScoresActivity.class);
				startActivity(intent_scores);
				return true;
			case R.id.opt_wordset:
				Intent intent_wordset = new Intent(MainActivity.this, WordSetActivity.class);
				startActivity(intent_wordset);
				return true;
			case R.id.opt_about:
				Intent intent_about = new Intent(MainActivity.this, AboutActivity.class);
				startActivity(intent_about);
				return true;
			case R.id.opt_manual:
                Intent intent_manual = new Intent(MainActivity.this, ManualActivity.class);
                startActivity(intent_manual);
                return true;
		}
		return super.onOptionsItemSelected(item);
	}


	/* Class: Countdown Timer */

	public class CountDownTimerExt extends CountDownTimer
	{
		long time_remaining;

		public CountDownTimerExt(long millisInFuture, long countDownInterval)
		{
			super(millisInFuture, countDownInterval);
			time_remaining = millisInFuture;
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
			time_remaining = millisUntilFinished;

			int time = (int) (millisUntilFinished / 1000);

			Button b = findViewById(R.id.button_timer);
			b.setText(String.format("%02d:%02d", time / 60, time % 60));

			if (time <= 10)
			{
				b.setTextColor(0xffff0000);

				final MediaPlayer mp = MediaPlayer.create(MainActivity.getContext(), R.raw.tick);
				mp.start();
				mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
				{
					public void onCompletion(MediaPlayer player)
					{
						player.reset();
						player.release();
					}
				});
			}
		}

		@Override
		public void onFinish()
		{
			Button b = findViewById(R.id.button_timer);
			b.setText(R.string.timer_start);
			b.setTextColor(0xffffffff);
			// Store achieved score and reset game.
			MainActivity.getInstance().storeScore();
			MainActivity.getInstance().resetGameState();
		}

		public long getTimeRemaining()
		{
			return time_remaining;
		}
	}

}
