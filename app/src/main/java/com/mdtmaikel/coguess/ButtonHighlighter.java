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


import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


public class ButtonHighlighter implements View.OnTouchListener
{
    private static final int TRANSPARENT_GREY = Color.argb(0, 185, 185, 185);
    private static final int FILTERED_GREY = Color.argb(155, 185, 185, 185);

    ImageButton image_button;
    ImageView image_view;
    TextView text_view;

    public ButtonHighlighter(final ImageButton imageButton)
    {
        super();
        this.image_button = imageButton;
    }

    public ButtonHighlighter(final ImageView imageView)
    {
        super();
        this.image_view = imageView;
    }

    public ButtonHighlighter(final TextView textView)
    {
        super();
        this.text_view = textView;
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent)
    {
        if (image_button != null)
        {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                image_button.setColorFilter(FILTERED_GREY);
            else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                image_button.setColorFilter(TRANSPARENT_GREY);
        }
        else if (image_view != null)
        {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                image_view.setColorFilter(FILTERED_GREY);
            else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                image_view.setColorFilter(TRANSPARENT_GREY);
        }
        else if (text_view != null)
        {
            for (final Drawable compoundDrawable : text_view.getCompoundDrawables())
            {
                if (compoundDrawable != null)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                        compoundDrawable.setColorFilter(FILTERED_GREY, PorterDuff.Mode.SRC_ATOP);
                    else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                        compoundDrawable.setColorFilter(TRANSPARENT_GREY, PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
        return false;
    }
}