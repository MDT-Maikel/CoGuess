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
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
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
        TextView about_donation_btc = (TextView) findViewById(R.id.about_donation_btc);
        about_donation_btc.setMovementMethod(LinkMovementMethod.getInstance());
        TextView about_donation_ltc = (TextView) findViewById(R.id.about_donation_ltc);
        about_donation_ltc.setMovementMethod(LinkMovementMethod.getInstance());
        TextView about_donation_zec = (TextView) findViewById(R.id.about_donation_zec);
        about_donation_zec.setMovementMethod(LinkMovementMethod.getInstance());

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

    public void buttonOnCopyBitcoinClick(View v)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("btc_address", getString(R.string.about_donation_btc_address));
        clipboard.setPrimaryClip(clip);
        AppUtility.displayToastShort(getApplicationContext(), R.string.about_donation_copied_address_btc);
    }

    public void buttonOnCopyLitecoinClick(View v)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ltc_address", getString(R.string.about_donation_ltc_address));
        clipboard.setPrimaryClip(clip);
        AppUtility.displayToastShort(getApplicationContext(), R.string.about_donation_copied_address_ltc);
    }

    public void buttonOnCopyZCashClick(View v)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("zec_address", getString(R.string.about_donation_zec_address));
        clipboard.setPrimaryClip(clip);
        AppUtility.displayToastShort(getApplicationContext(), R.string.about_donation_copied_address_zec);
    }
}