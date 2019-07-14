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


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Hashtable;


public class ScoresActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        // Change action bar.
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.custom_action_bar_scores);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Implement button highlighting.
        ImageButton button_delete = (ImageButton) findViewById(R.id.image_button_delete);
        button_delete.setOnTouchListener(new ButtonHighlighter(button_delete));

        // Create recycler view.
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_scores);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Get scores from main activity.
        ArrayList<Hashtable> score_data_set = (ArrayList<Hashtable>) MainActivity.getInstance().getSortedScores();

        // Transfer scores to recycler view.
        mAdapter = new ScoresRecyclerViewAdapter(score_data_set);
        recyclerView.setAdapter(mAdapter);

    }

    public void buttonOnDeleteClick(View v)
    {
        // Delete all scores after asking for confirmation.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.title_activity_scores);
        builder.setMessage(R.string.score_delete_confirmation);
        builder.setPositiveButton(R.string.delete_confirm,
            new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // Proceed with deletion.
                    deleteAllScores();
                }
            });
        builder.setNegativeButton(R.string.delete_cancel,
            new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // No need to do anything.
                }
            });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAllScores()
    {
        // Delete all scores.
        MainActivity.getInstance().deleteAllScores();

        // Get scores from main activity.
        ArrayList<Hashtable> score_data_set = (ArrayList<Hashtable>) MainActivity.getInstance().getSortedScores();

        // Transfer scores to recycler view.
        mAdapter = new ScoresRecyclerViewAdapter(score_data_set);
        recyclerView.setAdapter(mAdapter);
    }
}