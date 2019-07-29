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


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;


public class DBCustomWordSet extends SQLiteOpenHelper
{
    // Database entries.
    public static final String DATABASE_NAME = "custom_wordset_database.db";
    public static final String WORDSET_TABLE_NAME = "custom_wordset";
    public static final String WORDSET_COLUMN_ID = "_id";
    public static final String WORDSET_COLUMN_LANGUAGE = "language";
    public static final String WORDSET_COLUMN_WORD = "word";
    public static final String WORDSET_COLUMN_WORD_LIST_NAME = "word_list_name";
    public static final String WORDSET_LIST_NAME_IDENTIFIER = "$$list$$identifier$$";
    public static final String WORDSET_LIST_ACTIVE_IDENTIFIER = "$$list$$active$$";

    public DBCustomWordSet(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(
                "CREATE TABLE " + WORDSET_TABLE_NAME + " (" +
                        WORDSET_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        WORDSET_COLUMN_LANGUAGE + " text, " +
                        WORDSET_COLUMN_WORD + " text, " +
                        WORDSET_COLUMN_WORD_LIST_NAME + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + WORDSET_TABLE_NAME);
        onCreate(db);
    }


    /*-- Custom Word Methods --*/

    public boolean insertWord(String word, String language, String for_word_list)
    {
        if (isExistingEntry(word, language, for_word_list))
            return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(WORDSET_COLUMN_LANGUAGE, language);
        contentValues.put(WORDSET_COLUMN_WORD, word);
        contentValues.put(WORDSET_COLUMN_WORD_LIST_NAME, for_word_list);
        db.insert(WORDSET_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public boolean isExistingEntry(String word, String language, String for_word_list)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT EXISTS (SELECT 1 FROM " + WORDSET_TABLE_NAME + " WHERE " + WORDSET_COLUMN_WORD + " = ? AND " + WORDSET_COLUMN_LANGUAGE + " = ? AND " + WORDSET_COLUMN_WORD_LIST_NAME + " = ?)", new String[] { word, language, for_word_list });
        res.moveToFirst();
        if (res.getInt(0) == 1)
        {
            res.close();
            return true;
        }
        res.close();
        return false;
    }

    public void deleteWordByName(String word)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(WORDSET_TABLE_NAME, WORDSET_COLUMN_WORD + " = ? ", new String[] { word });
        db.close();

    }

    public void deleteWordByName(String word, String only_for_language)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(WORDSET_TABLE_NAME, WORDSET_COLUMN_WORD + " = ? AND " + WORDSET_COLUMN_LANGUAGE + " = ?", new String[] { word, only_for_language });
        db.close();
    }

    public void deleteWordByName(String word, String only_for_language, String only_for_word_list)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(WORDSET_TABLE_NAME, WORDSET_COLUMN_WORD + " = ? AND " + WORDSET_COLUMN_LANGUAGE + " = ? AND " + WORDSET_COLUMN_WORD_LIST_NAME + " = ?", new String[] { word, only_for_language, only_for_word_list });
        db.close();
    }

    public void deleteAllWords(String only_for_language)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(WORDSET_TABLE_NAME, WORDSET_COLUMN_LANGUAGE + " = ?", new String[] { only_for_language });
        db.close();
    }

    public void deleteAllWords(String only_for_language, String only_for_word_list)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(WORDSET_TABLE_NAME, WORDSET_COLUMN_LANGUAGE + " = ? AND " + WORDSET_COLUMN_WORD_LIST_NAME + " = ?", new String[] { only_for_language, only_for_word_list });
        db.close();
    }

    public ArrayList<String> getAllWords(String language, String for_word_list)
    {
        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT * FROM " + WORDSET_TABLE_NAME + " WHERE " + WORDSET_COLUMN_LANGUAGE + " = ? AND " + WORDSET_COLUMN_WORD_LIST_NAME + " = ?" , new String[] { language, for_word_list });
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            String word = cursor.getString(cursor.getColumnIndex(WORDSET_COLUMN_WORD));
            if (!word.equals(WORDSET_LIST_NAME_IDENTIFIER) && !word.equals(WORDSET_LIST_ACTIVE_IDENTIFIER))
                array_list.add(word);
            cursor.moveToNext();
        }
        db.close();
        cursor.close();
        return array_list;
    }


    /*-- Word List Methods --*/

    public boolean addWordList(String word_list_name, String language)
    {
        if (isExistingEntry(WORDSET_LIST_NAME_IDENTIFIER, language, word_list_name))
            return false;
        return insertWord(WORDSET_LIST_NAME_IDENTIFIER, language, word_list_name);
    }

    public ArrayList<String> getAllWordLists(String language)
    {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT * FROM " + WORDSET_TABLE_NAME + " WHERE " + WORDSET_COLUMN_WORD + " = ? AND " + WORDSET_COLUMN_LANGUAGE + " = ?" , new String[] { WORDSET_LIST_NAME_IDENTIFIER, language });
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            String word_list = cursor.getString(cursor.getColumnIndex(WORDSET_COLUMN_WORD_LIST_NAME));
            array_list.add(word_list);
            cursor.moveToNext();
        }
        // Sort word list alphabetically.
        Collections.sort(array_list, String.CASE_INSENSITIVE_ORDER);
        db.close();
        cursor.close();
        return array_list;
    }

    public void setWordListActive(String word_list_name, String language, boolean active)
    {
        if (active)
        {
            if (!isExistingEntry(WORDSET_LIST_ACTIVE_IDENTIFIER, language, word_list_name))
                insertWord(WORDSET_LIST_ACTIVE_IDENTIFIER, language, word_list_name);
        }
        else
        {
            deleteWordByName(WORDSET_LIST_ACTIVE_IDENTIFIER, language, word_list_name);
        }
    }

    public boolean getWordListActive(String word_list_name, String language)
    {
        return isExistingEntry(WORDSET_LIST_ACTIVE_IDENTIFIER, language, word_list_name);
    }

    public ArrayList<String> getAllActivatedWords(String language)
    {
        ArrayList<String> array_list = new ArrayList<String>();
        // Get active word lists for this language.
        ArrayList<String> active_word_lists = getAllWordLists(language);
        active_word_lists.removeIf(word_list -> !getWordListActive(word_list, language));

        // Loop over all active word lists.
        for (String word_list : active_word_lists)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor =  db.rawQuery( "SELECT * FROM " + WORDSET_TABLE_NAME + " WHERE " + WORDSET_COLUMN_LANGUAGE + " = ? AND " + WORDSET_COLUMN_WORD_LIST_NAME + " = ?" , new String[] { language, word_list });
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                String word = cursor.getString(cursor.getColumnIndex(WORDSET_COLUMN_WORD));
                if (!word.equals(WORDSET_LIST_NAME_IDENTIFIER) && !word.equals(WORDSET_LIST_ACTIVE_IDENTIFIER))
                    array_list.add(word);
                cursor.moveToNext();
            }
            db.close();
            cursor.close();
        }
        return array_list;
    }
}