package com.bence.songbook.ui.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bence.songbook.models.Song;
import com.bence.songbook.ui.fragment.SongFragment;

import java.util.List;

/**
 * Created by abdalla on 2/18/18.
 */

public class PageAdapter extends FragmentPagerAdapter {

    private final List<Song> songs;
    private int numOfTabs;

    public PageAdapter(FragmentManager fm, int numOfTabs, List<Song> songs) {
        super(fm);
        this.numOfTabs = numOfTabs;
        this.songs = songs;
    }

    @Override
    public Fragment getItem(int position) {
        if (position < 0) {
            return null;
        }
        if (songs.size() > position) {
            return new SongFragment().setSong(songs.get(position));
        }
        return new SongFragment();
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
