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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.service.SongService;
import com.bence.songbook.ui.utils.Preferences;

import java.util.ArrayList;
import java.util.List;

public class SongActivity extends AppCompatActivity {

    private Song song;
    private Memory memory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        memory = Memory.getInstance();
        setContentView(R.layout.activity_song);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Intent intent = getIntent();
        song = (Song) intent.getSerializableExtra("Song");
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
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            setBlank();
        } else if (itemId == R.id.action_similar) {
            List<Song> allSimilar = SongService.findAllSimilar(song, memory.getSongs());
            memory.setValues(allSimilar);
            if (allSimilar.size() > 0) {
                setResult(1);
                finish();
            } else {
                Toast.makeText(this, "No similar found", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.action_suggest_edits) {
            Intent intent = new Intent(this, SuggestEditsActivity.class);
            Song copiedSong = new Song();
            copiedSong.setUuid(song.getUuid());
            copiedSong.setId(song.getId());
            copiedSong.setTitle(song.getTitle());
            copiedSong.setVerses(song.getVerses());
            copiedSong.setSongCollection(song.getSongCollection());
            copiedSong.setSongCollectionElement(song.getSongCollectionElement());
            intent.putExtra("Song", copiedSong);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setBlank();
    }

    private void setBlank() {
        if (memory.isShareOnNetwork()) {
            List<ProjectionTextChangeListener> projectionTextChangeListeners = memory.getProjectionTextChangeListeners();
            if (projectionTextChangeListeners != null) {
                for (int i = 0; i < projectionTextChangeListeners.size(); ++i) {
                    projectionTextChangeListeners.get(i).onSetText("");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.content_song_menu, menu);
        MenuItem item = menu.findItem(R.id.action_similar);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean show_similar = sharedPreferences.getBoolean("show_similar", false);
        if (!show_similar) {
            item.setVisible(false);
            menu.removeItem(item.getItemId());
        }
        return true;
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
