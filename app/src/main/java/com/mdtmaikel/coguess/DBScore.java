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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;


public class DBScore extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "score_database.db";
    public static final String SCORE_TABLE_NAME = "scores";
    public static final String SCORE_COLUMN_ID = "_id";
    public static final String SCORE_COLUMN_NAME = "name";
    public static final String SCORE_COLUMN_SCORE = "score";
    public static final String SCORE_COLUMN_LANGUAGE = "language";
    public static final String SCORE_COLUMN_DURATION = "duration";
    public static final String SCORE_COLUMN_WORDSET = "wordset";

    public DBScore(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(
                "CREATE TABLE " + SCORE_TABLE_NAME + " (" +
                        SCORE_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        SCORE_COLUMN_NAME + " text, " +
                        SCORE_COLUMN_SCORE + " INTEGER, " +
                        SCORE_COLUMN_LANGUAGE + " text, " +
                        SCORE_COLUMN_DURATION + " INTEGER, " +
                        SCORE_COLUMN_WORDSET + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + SCORE_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertScore(String name, int score, String language, int duration, String wordset)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SCORE_COLUMN_NAME, name);
        contentValues.put(SCORE_COLUMN_SCORE, score);
        contentValues.put(SCORE_COLUMN_LANGUAGE, language);
        contentValues.put(SCORE_COLUMN_DURATION, duration);
        contentValues.put(SCORE_COLUMN_WORDSET, wordset);
        db.insert(SCORE_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public Integer deleteScore(Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(SCORE_TABLE_NAME, SCORE_COLUMN_ID + " = ? ", new String[] { Integer.toString(id) });
    }

    public void deleteAllScores()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + SCORE_TABLE_NAME);
        onCreate(db);
        db.close();
    }


    public ArrayList<Hashtable> getAllScores()
    {
        ArrayList<Hashtable> array_list = new ArrayList<Hashtable>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT * FROM " + SCORE_TABLE_NAME, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            String name = cursor.getString(cursor.getColumnIndex(SCORE_COLUMN_NAME));
            String score = cursor.getString(cursor.getColumnIndex(SCORE_COLUMN_SCORE));
            Hashtable<String, String> score_table = new Hashtable<String, String>();
            score_table.put("team_name", name);
            score_table.put("score", score);
            array_list.add(score_table);
            cursor.moveToNext();
        }
        db.close();
        cursor.close();
        return array_list;
    }

    public ArrayList<Hashtable> getAllSortedScores()
    {
        ArrayList<Hashtable> array_list = getAllScores();
        Collections.sort(array_list, new SortByScore());
        return array_list;
    }

    class SortByScore implements Comparator<Hashtable>
    {
        public int compare(Hashtable a, Hashtable b)
        {
            // Sort score descendingly.
            return Integer.parseInt((String) b.get("score")) - Integer.parseInt((String) a.get("score"));
        }
    }
}