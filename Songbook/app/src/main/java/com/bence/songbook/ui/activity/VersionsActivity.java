package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.repository.SongCollectionRepository;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.impl.ormLite.SongCollectionRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VersionsActivity extends AppCompatActivity {

    public static Song getSongFromMemory(Song song) {
        List<Song> songs = Memory.getInstance().getSongs();
        if (songs == null) {
            return song;
        }
        for (Song iSong : songs) {
            if (iSong != null && iSong.getUuid().equals(song.getUuid())) {
                return iSong;
            }
        }
        return song;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        SongRepository songRepository = new SongRepositoryImpl(this);
        Song passingSong = Memory.getInstance().getPassingSong();
        if (passingSong != null) {
            String versionGroup = passingSong.getVersionGroup();
            String uuid = passingSong.getUuid();
            if (versionGroup == null) {
                versionGroup = uuid;
            }
            List<Song> allByVersionGroup = songRepository.findAllByVersionGroup(versionGroup);
            final List<Song> songs = new ArrayList<>(allByVersionGroup.size());
            HashMap<String, Song> hashMap = new HashMap<>(songs.size());
            for (Song song : allByVersionGroup) {
                if (!song.getUuid().equals(uuid)) {
                    hashMap.put(song.getUuid(), getSongFromMemory(song));
                }
            }
            SongCollectionRepository songCollectionRepository = new SongCollectionRepositoryImpl(this);
            List<SongCollection> songCollections = songCollectionRepository.findAll();
            for (SongCollection songCollection : songCollections) {
                for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
                    String songUuid = songCollectionElement.getSongUuid();
                    if (hashMap.containsKey(songUuid)) {
                        Song song = hashMap.get(songUuid);
                        song.setSongCollection(songCollection);
                        song.setSongCollectionElement(songCollectionElement);
                        songs.add(song);
                        hashMap.remove(songUuid);
                    }
                }
            }
            songs.addAll(hashMap.values());
            SongAdapter adapter = new SongAdapter(this, R.layout.content_song_list_row, songs);
            ListView songListView = findViewById(R.id.listView);
            songListView.setAdapter(adapter);
            songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Song tmp = songs.get(position);
                    showSongFullscreen(tmp);
                }

            });
        }
    }

    public void showSongFullscreen(Song song) {
        Memory.getInstance().setPassingSong(song);
        setResult(1);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class SongAdapter extends ArrayAdapter<Song> {

        private List<Song> songList;

        SongAdapter(Context context, int textViewResourceId,
                    List<Song> songList) {
            super(context, textViewResourceId, songList);
            this.songList = new ArrayList<>();
            this.songList.addAll(songList);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @SuppressWarnings("ConstantConditions")
        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

            SongAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.content_song_list_row, null);

                holder = new SongAdapter.ViewHolder();
                holder.ordinalNumberTextView = convertView.findViewById(R.id.ordinalNumberTextView);
                holder.titleTextView = convertView.findViewById(R.id.titleTextView);
                holder.imageView = convertView.findViewById(R.id.starImageView);
                convertView.setTag(holder);
            } else {
                holder = (SongAdapter.ViewHolder) convertView.getTag();
            }

            Song song = songList.get(position);
            holder.imageView.setVisibility(song.isFavourite() ? View.VISIBLE : View.INVISIBLE);
            SongCollection songCollection = song.getSongCollection();
            if (songCollection != null) {
                String collectionName = songCollection.getName();
                String text = collectionName + " " + song.getSongCollectionElement().getOrdinalNumber();
                holder.ordinalNumberTextView.setText(text);
            } else {
                holder.ordinalNumberTextView.setText("");
            }
            holder.titleTextView.setText(song.getTitle());
            holder.titleTextView.setTag(song);

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView ordinalNumberTextView;
            TextView titleTextView;
            View imageView;
        }

    }
}
