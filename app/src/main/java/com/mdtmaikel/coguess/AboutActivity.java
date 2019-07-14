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


import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class AboutActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Update application information.
        TextView about_description = (TextView) findViewById(R.id.about_description);
        String language_word_counts = MainActivity.getInstance().getWordSetDatabase().getWordCountPerLanguage();
        about_description.setText(String.format(getResources().getString(R.string.about_description), language_word_counts));

        // Make hyperlinks work.
        TextView about_license = (TextView) findViewById(R.id.about_license);
        about_license.setMovementMethod(LinkMovementMethod.getInstance());
        TextView about_feedback = (TextView) findViewById(R.id.about_feedback);
        about_feedback.setMovementMethod(LinkMovementMethod.getInstance());
        TextView about_donation = (TextView) findViewById(R.id.about_donation);
        about_donation.setMovementMethod(LinkMovementMethod.getInstance());

        // Change action bar.
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.custom_action_bar_about);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}