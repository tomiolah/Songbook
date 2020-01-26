package com.bence.songbook.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bence.songbook.R;
import com.bence.songbook.models.SongVerse;

class SectionTypeViewHolder extends RecyclerView.ViewHolder {

    TextView textView;

    SectionTypeViewHolder(View v) {
        super(v);
        textView = v.findViewById(R.id.textView);
    }

    void bind(final SongVerse songVerse, final int position) {
    }

}
