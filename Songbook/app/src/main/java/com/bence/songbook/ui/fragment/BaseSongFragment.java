package com.bence.songbook.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bence.songbook.R;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseSongFragment extends Fragment {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    Song song;
    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(false);
        View view = inflater.inflate(R.layout.content_song, container, false);
        this.view = view;
        if (song != null) {
            loadSongView();
        }
        return view;
    }

    private void loadSongView() {
        TextView collectionTextView = view.findViewById(R.id.collectionTextView);
        if (song.getSongCollection() != null) {
            String text = song.getSongCollection().getName() + " " + song.getSongCollectionElement().getOrdinalNumber();
            collectionTextView.setText(text);
            collectionTextView.setVisibility(View.VISIBLE);
        } else {
            collectionTextView.setVisibility(View.GONE);
        }

        MyCustomAdapter dataAdapter = new MyCustomAdapter(getActivity(),
                R.layout.content_song_verse, song.getVerses());
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onSongVerseClick(position);
            }

        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView v, int scrollState) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    @SuppressWarnings("ConstantConditions")
                    View view = BaseSongFragment.this.getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) BaseSongFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    public Fragment setSong(Song song) {
        this.song = song;
        if (view != null) {
            loadSongView();
        }
        return this;
    }

    protected abstract void onSongVerseClick(int position);

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            ViewGroup viewGroup = (ViewGroup) getView();
            if (viewGroup != null) {
                viewGroup.removeAllViewsInLayout();
                View view = onCreateView(inflater, viewGroup, null);
                viewGroup.addView(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void setText(SongVerse songVerse, TextView textView);

    @SuppressWarnings("ConstantConditions")
    private class MyCustomAdapter extends ArrayAdapter<SongVerse> {

        private List<SongVerse> songVerses;

        MyCustomAdapter(Context context, int textViewResourceId,
                        List<SongVerse> songVerses) {
            super(context, textViewResourceId, songVerses);
            this.songVerses = new ArrayList<>();
            this.songVerses.addAll(songVerses);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            MyCustomAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.content_song_verse, null);

                holder = new MyCustomAdapter.ViewHolder();
                holder.textView = convertView.findViewById(R.id.textView);
                holder.chorusTextView = convertView.findViewById(R.id.chorusTextView);
                convertView.setTag(holder);
            } else {
                holder = (MyCustomAdapter.ViewHolder) convertView.getTag();
            }

            SongVerse songVerse = songVerses.get(position);
            setText(songVerse, holder.textView);
            if (!songVerse.isChorus()) {
                holder.chorusTextView.setVisibility(View.GONE);
            } else {
                holder.chorusTextView.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        private class ViewHolder {
            TextView textView;
            TextView chorusTextView;
        }

    }
}