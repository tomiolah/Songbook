package com.bence.songbook.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bence.songbook.R;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.ui.utils.Preferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewSongActivity extends AppCompatActivity {

    private Spinner languageSpinner;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_song);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.new_song);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        View editText = findViewById(R.id.text);
        editText.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                }
                v.performClick();
                return false;
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        EditText emailEditText = findViewById(R.id.email);
        emailEditText.setText(sharedPreferences.getString("email", ""));

        LanguageRepositoryImpl languageRepository = new LanguageRepositoryImpl(this);
        final List<Language> languages = languageRepository.findAll();
        languageSpinner = findViewById(R.id.languageSpinner);
        final List<String> spinnerArray = new ArrayList<>();
        for (Language language : languages) {
            spinnerArray.add(language.getNativeName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
        if (languages.size() > 0) {
            languageSpinner.setSelection(0, true);
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createSong(languages);
            }
        });
    }

    private void createSong(List<Language> languages) {
        Song song = new Song();
        EditText titleEditText = findViewById(R.id.title);
        String title = titleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.no_title, Toast.LENGTH_SHORT).show();
            return;
        }
        song.setTitle(title);
        EditText editText = findViewById(R.id.text);
        String text = editText.getText().toString();
        String replaceAll = text.replaceAll("\n\n\n", "\n\n");
        while (replaceAll.contains("\n\n\n")) {
            replaceAll = replaceAll.replaceAll("\n\n\n", "\n\n");
        }
        if (replaceAll.isEmpty()) {
            Toast.makeText(this, R.string.empty_verses, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] split = replaceAll.split("\n\n");
        List<SongVerse> songVerses = new ArrayList<>(split.length);
        for (String verse : split) {
            SongVerse songVerse = new SongVerse();
            songVerse.setText(verse.trim());
            songVerses.add(songVerse);
        }
        song.setVerses(songVerses);

        EditText emailEditText = findViewById(R.id.email);
        String email = emailEditText.getText().toString().trim();
        sharedPreferences.edit().putString("email", email).apply();
        song.setCreatedDate(new Date());
        song.setModifiedDate(song.getCreatedDate());
        song.setLanguage(languages.get(languageSpinner.getSelectedItemPosition()));
        SongRepository songRepository = new SongRepositoryImpl(this);
        songRepository.save(song);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
