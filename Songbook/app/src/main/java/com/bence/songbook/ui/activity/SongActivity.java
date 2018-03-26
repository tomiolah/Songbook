package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.ui.utils.Preferences;

import java.util.ArrayList;
import java.util.List;

public class SongActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Intent intent = getIntent();
        Song song = (Song) intent.getSerializableExtra("Song");
        toolbar.setTitle(song.getTitle());
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        final Intent fullScreenIntent = new Intent(this, FullscreenActivity.class);
        fullScreenIntent.putExtra("Song", song);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenIntent.putExtra("verseIndex", 0);
                startActivity(fullScreenIntent);
            }
        });

        TextView collectionTextView = findViewById(R.id.collectionTextView);
        if (song.getSongCollection() != null) {
            String text = song.getSongCollection().getName() + " " + song.getSongCollectionElement().getOrdinalNumber();
            collectionTextView.setText(text);
        } else {
            collectionTextView.setVisibility(View.GONE);
        }

        MyCustomAdapter dataAdapter = new MyCustomAdapter(this,
                R.layout.content_song_verse, song.getVerses());
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean show_title_switch = sharedPreferences.getBoolean("show_title_switch", false);
                fullScreenIntent.putExtra("verseIndex", position + (show_title_switch ? 1 : 0));
                startActivity(fullScreenIntent);
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            setBlank();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setBlank();
    }

    private void setBlank() {
        Memory memory = Memory.getInstance();
        if (memory.isShareOnNetwork()) {
            List<ProjectionTextChangeListener> projectionTextChangeListeners = memory.getProjectionTextChangeListeners();
            if (projectionTextChangeListeners != null) {
                for (int i = 0; i < projectionTextChangeListeners.size(); ++i) {
                    projectionTextChangeListeners.get(i).onSetText("");
                }
            }
        }
    }

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
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
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
            holder.textView.setText(songVerse.getText());
            if (!songVerse.isChorus()) {
                holder.chorusTextView.setVisibility(View.GONE);
            }
            return convertView;
        }

        private class ViewHolder {
            TextView textView;
            TextView chorusTextView;
        }

    }
}
