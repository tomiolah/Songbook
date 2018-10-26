package com.bence.songbook.ui.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.bence.songbook.models.Song;
import com.bence.songbook.ui.fragment.SongFragment;

import java.util.ArrayList;
import java.util.List;

public class PageAdapter extends FragmentStatePagerAdapter {

    private List<Song> songs;
    private List<SongFragment> fragments;

    public PageAdapter(FragmentManager fm, List<Song> songs) {
        super(fm);
        this.songs = songs;
        fragments = new ArrayList<>(songs.size());
        for (int i = 0; i < songs.size(); ++i) {
            fragments.add(null);
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (position < 0) {
            return null;
        }
        if (songs.size() > position) {
            SongFragment fragment = new SongFragment().setSong(songs.get(position));
            fragments.set(position, fragment);
            return fragment;
        }
        return new SongFragment();
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        SongFragment ret = (SongFragment) super.instantiateItem(container, position);
        if (ret.getSong() == null) {
            SongFragment songFragment = fragments.get(position);
            if (songFragment != null) {
                return songFragment;
            } else {
                return getItem(position);
            }
        }
        return ret;
    }
}
