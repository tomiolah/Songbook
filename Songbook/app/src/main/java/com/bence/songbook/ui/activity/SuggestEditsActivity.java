package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bence.projector.common.dto.SongVerseDTO;
import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.songbook.R;
import com.bence.songbook.api.SuggestionApiBean;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.ui.utils.Preferences;

import java.util.ArrayList;
import java.util.List;

public class SuggestEditsActivity extends AppCompatActivity {

    private Song song;
    private boolean edit = false;
    private EditText titleEditText;
    private EditText textEditText;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_edits);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.new_song);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
        Intent intent = getIntent();
        song = (Song) intent.getSerializableExtra("Song");
        String method = intent.getStringExtra("method");
        titleEditText = findViewById(R.id.title);
        textEditText = findViewById(R.id.text);
        titleEditText.setText(song.getTitle());
        textEditText.setText(getText(song));
        if (method.equals("EDIT")) {
            edit = true;
        } else {
            titleEditText.setKeyListener(null);
            titleEditText.setFocusable(false);
            titleEditText.setCursorVisible(false);
            textEditText.setKeyListener(null);
            textEditText.setFocusable(false);
            textEditText.setCursorVisible(false);
        }
    }

    private String getText(Song song) {
        StringBuilder text = new StringBuilder();
        for (SongVerse songVerse : song.getVerses()) {
            if (text.length() > 0) {
                text.append("\n\n");
            }
            text.append(songVerse.getText().trim());
        }
        return text.toString();
    }

    private void submit() {
        final SuggestionDTO suggestionDTO = new SuggestionDTO();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String email = sharedPreferences.getString("email", "");
        suggestionDTO.setCreatedByEmail(email);
        final EditText suggestionEditText = findViewById(R.id.suggestion);
        String description = suggestionEditText.getText().toString().trim();
        if (description.isEmpty() && !edit) {
            Toast.makeText(this, R.string.no_description, Toast.LENGTH_SHORT).show();
            return;
        }
        suggestionDTO.setDescription(description);
        suggestionDTO.setSongId(song.getUuid());
        if (edit) {
            String title = titleEditText.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, R.string.no_title, Toast.LENGTH_SHORT).show();
                return;
            }
            suggestionDTO.setTitle(title);
            String[] split = textEditText.getText().toString().trim().split("\n\n");
            List<SongVerseDTO> songVerseDTOList = new ArrayList<>(song.getVerses().size());
            for (String s : split) {
                SongVerseDTO songVerseDTO = new SongVerseDTO();
                songVerseDTO.setText(s);
                songVerseDTOList.add(songVerseDTO);
            }
            if (songVerseDTOList.size() == 0 || (songVerseDTOList.size() == 1 && songVerseDTOList.get(0).getText().isEmpty())) {
                Toast.makeText(this, R.string.empty_verses, Toast.LENGTH_SHORT).show();
                return;
            }
            suggestionDTO.setVerses(songVerseDTOList);
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SuggestionApiBean suggestionApiBean = new SuggestionApiBean();
                final SuggestionDTO uploadedSuggestion = suggestionApiBean.uploadSuggestion(suggestionDTO);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (uploadedSuggestion != null && !uploadedSuggestion.getUuid().trim().isEmpty()) {
                            song.setUuid(uploadedSuggestion.getUuid());
                            Toast.makeText(SuggestEditsActivity.this, R.string.successfully_uploaded, Toast.LENGTH_SHORT).show();
                            setResult(1);
                            finish();
                        } else {
                            Toast.makeText(SuggestEditsActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        thread.start();
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
