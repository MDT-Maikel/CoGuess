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


import android.content.Context;
import android.text.SpannedString;
import android.view.Gravity;
import android.widget.Toast;


public class AppUtility
{
    // Returns language name according to language/country code.
    public static String LanguageIDtoString(String id)
    {
        switch(id)
        {
            case "en": return "English";
            case "de": return "German";
            case "nl": return "Dutch";
        }
        return "Unknown Language";
    }


    /*-- Toast Wrappers --*/

    public static void displayToastLong(Context ctx, String msg)
    {
        Toast toast = Toast.makeText(ctx, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void displayToastShort(Context ctx, String msg)
    {
        Toast toast = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void displayToastLong(Context ctx, SpannedString msg)
    {
        Toast toast = Toast.makeText(ctx, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void displayToastShort(Context ctx, SpannedString msg)
    {
        Toast toast = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void displayToastLong(Context ctx, int msg_id)
    {
        Toast toast = Toast.makeText(ctx, msg_id, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void displayToastShort(Context ctx, int msg_id)
    {
        Toast toast = Toast.makeText(ctx, msg_id, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}

