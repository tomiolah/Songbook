package com.bence.songbook.ui.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bence.songbook.models.Song;
import com.bence.songbook.ui.fragment.MainSongFragment;

import java.util.List;

public class MainPageAdapter extends FragmentStatePagerAdapter {

    private List<Song> songs;

    public MainPageAdapter(FragmentManager fm, List<Song> songs) {
        super(fm);
        this.songs = songs;
    }

    @Override
    public Fragment getItem(int position) {
        if (position < 0) {
            return null;
        }
        if (songs.size() > position) {
            return new MainSongFragment().setSong(songs.get(position));
        }
        return new MainSongFragment();
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
