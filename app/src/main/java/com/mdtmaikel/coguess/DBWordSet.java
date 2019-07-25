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
import android.database.DatabaseUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class DBWordSet extends SQLiteOpenHelper
{
    // Database entries.
    public static final String DATABASE_NAME = "wordset_database.db";
    public static final String WORDSET_TABLE_NAME = "wordset";
    public static final String WORDSET_COLUMN_ID = "_id";
    public static final String WORDSET_COLUMN_LANGUAGE = "language";
    public static final String WORDSET_COLUMN_WORD = "word";
    public static final String WORDSET_COLUMN_DIFFICULTY = "difficulty";
    public static final String WORDSET_COLUMN_CATEGORY = "category";

    private final Context db_context;
    private static String DATABASE_PATH = "";

    public DBWordSet(Context context)
    {
        super(context, DATABASE_NAME, null, 1);

        if (android.os.Build.VERSION.SDK_INT >= 17)
            DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DATABASE_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.db_context = context;

        copyDataBase();

        this.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // No need to init an already loaded table.
        /*db.execSQL(
                "CREATE TABLE " + WORDSET_TABLE_NAME + " (" +
                        WORDSET_COLUMN_ID + " integer PRIMARY KEY, " +
                        WORDSET_COLUMN_LANGUAGE + " text, " +
                        WORDSET_COLUMN_WORD + " text, " +
                        WORDSET_COLUMN_DIFFICULTY + " INTEGER, " +
                        WORDSET_COLUMN_CATEGORY + " INTEGER)"
        );*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + WORDSET_TABLE_NAME);
        onCreate(db);
    }

    public void updateDataBase() throws IOException
    {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        if (dbFile.exists())
            dbFile.delete();

        copyDataBase();
    }

    private boolean checkDataBase()
    {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        return dbFile.exists();
    }

    private void copyDataBase()
    {
        if (!checkDataBase())
        {
            this.getReadableDatabase();
            this.close();
            try
            {
                copyDBFile();
            }
            catch (IOException mIOException)
            {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException
    {
        InputStream input = db_context.getResources().openRawResource(R.raw.initial_wordset_database);
        OutputStream output = new FileOutputStream(DATABASE_PATH + DATABASE_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = input.read(mBuffer)) > 0)
            output.write(mBuffer, 0, mLength);
        output.flush();
        output.close();
        input.close();
    }

    public boolean insertWord(String word, String language, int difficulty, int category)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(WORDSET_COLUMN_LANGUAGE, language);
        contentValues.put(WORDSET_COLUMN_WORD, word);
        contentValues.put(WORDSET_COLUMN_DIFFICULTY, difficulty);
        contentValues.put(WORDSET_COLUMN_CATEGORY, category);
        db.insert(WORDSET_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public boolean isExistingEntry(String word, String language)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT EXISTS (SELECT 1 FROM " + WORDSET_TABLE_NAME + " WHERE " + WORDSET_COLUMN_WORD + " = ? AND " + WORDSET_COLUMN_LANGUAGE + " = ?)", new String[] { word, language });
        res.moveToFirst();
        if (res.getInt(0) == 1)
        {
            res.close();
            return true;
        }
        res.close();
        return false;
    }

    public ArrayList<String> getAllWords(String language)
    {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT * FROM " + WORDSET_TABLE_NAME + " WHERE " + WORDSET_COLUMN_LANGUAGE + " = ?" , new String[] { language });
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            String word = cursor.getString(cursor.getColumnIndex(WORDSET_COLUMN_WORD));
            array_list.add(word);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return array_list;
    }

    public ArrayList<String> getAllWords(String language, int difficulty, int category)
    {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT * FROM " + WORDSET_TABLE_NAME + " WHERE " + WORDSET_COLUMN_LANGUAGE + " = ?" , new String[] { language });
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            String word = cursor.getString(cursor.getColumnIndex(WORDSET_COLUMN_WORD));
            int d = cursor.getInt(cursor.getColumnIndex(WORDSET_COLUMN_DIFFICULTY));
            int c = cursor.getInt(cursor.getColumnIndex(WORDSET_COLUMN_CATEGORY));
            if ((d & difficulty) != 0 && (c & category) != 0)
                array_list.add(word);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return array_list;
    }

    public long getWordCountForLanguage(String language)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, WORDSET_TABLE_NAME, WORDSET_COLUMN_LANGUAGE + " = ?", new String[] { language });
        db.close();
        return count;
    }

    public String getWordCountPerLanguage()
    {
        String info = "";
        String[] languages = new String[] {"en", "de", "nl"};
        for (String lan : languages)
            info = info + " * " + AppUtility.LanguageIDtoString(lan) + ":\t" + getWordCountForLanguage(lan) + " words,\n";
        return info.substring(0, info.length() - 2) + ".";
    }
}