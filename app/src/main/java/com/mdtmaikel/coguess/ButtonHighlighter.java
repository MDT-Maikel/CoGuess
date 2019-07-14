// TODO: where did I get this code from?

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

    ImageButton imageButton;
    ImageView imageView;
    TextView textView;

    public ButtonHighlighter(final ImageButton imageButton)
    {
        super();
        this.imageButton = imageButton;
    }

    public ButtonHighlighter(final ImageView imageView)
    {
        super();
        this.imageView = imageView;
    }

    public ButtonHighlighter(final TextView textView)
    {
        super();
        this.textView = textView;
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent)
    {
        if (imageButton != null)
        {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {
                imageButton.setColorFilter(FILTERED_GREY);
            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            {
                imageButton.setColorFilter(TRANSPARENT_GREY); // or null
            }
        }
        else if (imageView != null)
        {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {
                imageView.setColorFilter(FILTERED_GREY);
            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            {
                imageView.setColorFilter(TRANSPARENT_GREY); // or null
            }
        }
        else if (textView != null)
        {
            for (final Drawable compoundDrawable : textView.getCompoundDrawables())
            {
                if (compoundDrawable != null)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        // we use PorterDuff.Mode. SRC_ATOP as our filter color is already transparent
                        // we should have use PorterDuff.Mode.LIGHTEN with a non transparent color
                        compoundDrawable.setColorFilter(FILTERED_GREY, PorterDuff.Mode.SRC_ATOP);
                    }
                    else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    {
                        compoundDrawable.setColorFilter(TRANSPARENT_GREY, PorterDuff.Mode.SRC_ATOP); // or null
                    }
                }
            }
        }
        return false;
    }
}