package com.bence.songbook.ui.fragment;

import android.content.Intent;

import com.bence.songbook.Memory;
import com.bence.songbook.ui.activity.SongActivity;

public class MainSongFragment extends BaseSongFragment {
    @Override
    protected void onSongVerseClick(int position) {
        final Intent intent = new Intent(getActivity(), SongActivity.class);
        Memory.getInstance().setPassingSong(song);
        intent.putExtra("verseIndex", 0);
        startActivityForResult(intent, 3);
    }
}
