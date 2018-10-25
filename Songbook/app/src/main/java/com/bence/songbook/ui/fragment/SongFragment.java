package com.bence.songbook.ui.fragment;

import android.content.Intent;

import com.bence.songbook.ui.activity.FullscreenActivity;

public class SongFragment extends BaseSongFragment {

    @Override
    protected void onSongVerseClick(int position) {
        final Intent fullScreenIntent = new Intent(getActivity(), FullscreenActivity.class);
        fullScreenIntent.putExtra("verseIndex", position);
        startActivity(fullScreenIntent);
    }

}
