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
    private ArrayList<String> m_dataset;

    private OnItemClickListener m_listener;
    public interface OnItemClickListener
    {
        public void onItemClick(View view, String word);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        public View view;
        public MyViewHolder(View v)
        {
            super(v);
            view = v;
        }
    }

    public WordSetRecyclerViewAdapter(ArrayList<String> custom_word_list)
    {
        m_dataset = custom_word_list;
    }

    // Create new views (called by the layout manager).
    @Override
    public WordSetRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // Create a new view.
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view_recycler_wordset, parent,false);
        return new MyViewHolder(itemView);
    }

    // Replace the contents of a view (called by the layout manager).
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        TextView tv_word = (TextView) holder.view.findViewById(R.id.wordset_entry_word);
        final String word = (String) (m_dataset.get(position));
        tv_word.setText(word);

        holder.view.findViewById(R.id.wordset_entry_delete).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (m_listener != null)
                {
                    m_listener.onItemClick(view, word);
                }
            }
        });

    }

    // Return the size of your dataset (called by the layout manager).
    @Override
    public int getItemCount()
    {
        return m_dataset.size();
    }

    public void setOnItemClick(OnItemClickListener listener)
    {
        this.m_listener = listener;
    }
}