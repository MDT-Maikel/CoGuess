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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class WordSetRecyclerViewAdapter extends RecyclerView.Adapter<WordSetRecyclerViewAdapter.MyViewHolder>
{
    private ArrayList<String> mDataset;

    private OnItemClickListener mListener;
    public interface OnItemClickListener
    {
        public void onItemClick(View view, String word);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        // each data item is just a string in this case
        public View view;
        public MyViewHolder(View v)
        {
            super(v);
            view = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public WordSetRecyclerViewAdapter(ArrayList<String> custom_word_list)
    {
        mDataset = custom_word_list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public WordSetRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_view_recycler_wordset, parent, false);

        return new MyViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        TextView tv_word = (TextView) holder.view.findViewById(R.id.wordset_entry_word);
        final String word = (String) (mDataset.get(position));
        tv_word.setText(word);

        holder.view.findViewById(R.id.wordset_entry_delete).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mListener != null)
                {
                    mListener.onItemClick(view, word);
                }
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }

    public void setOnItemClick(OnItemClickListener listener)
    {
        this.mListener = listener;
    }
}