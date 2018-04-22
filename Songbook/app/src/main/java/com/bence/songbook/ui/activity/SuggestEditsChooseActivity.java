package com.bence.songbook.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bence.projector.common.dto.SongLinkDTO;
import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.SongLinkApiBean;
import com.bence.songbook.models.Song;
import com.bence.songbook.ui.utils.Preferences;

public class SuggestEditsChooseActivity extends AppCompatActivity {
    public final static int LINKING = 2;
    private Memory memory = Memory.getInstance();
    private Song song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_choose);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Button linkButton = findViewById(R.id.linkButton);
        Intent intent = getIntent();
        song = (Song) intent.getSerializableExtra("Song");
        if (song.getUuid() == null || song.getUuid().isEmpty()) {
            linkButton.setVisibility(View.GONE);
        } else if (memory.getSongForLinking() != null) {
            linkButton.setText(R.string.link_with_this_song);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onEditButtonClick(View view) {
        Intent intent = getIntent();
        intent.putExtra("method", "EDIT");
        intent.setClass(this, SuggestEditsActivity.class);
        startActivity(intent);
    }

    public void onOtherButtonClick(View view) {
        Intent intent = getIntent();
        intent.putExtra("method", "OTHER");
        intent.setClass(this, SuggestEditsActivity.class);
        startActivity(intent);
    }

    public void onLinkButtonClick(View view) {
        Song songForLinking = memory.getSongForLinking();
        if (songForLinking == null) {
            memory.setSongForLinking(song);
            Toast.makeText(this, "Select a version for the song", Toast.LENGTH_LONG).show();
            setResult(LINKING);
        } else {
            submit(songForLinking, song);
            memory.setSongForLinking(null);
        }
        finish();
    }

    private void submit(Song songForLinking, Song song) {
        final SongLinkDTO songLinkDTO = new SongLinkDTO();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String email = sharedPreferences.getString("email", "");
        songLinkDTO.setCreatedByEmail(email);
        songLinkDTO.setSongId1(songForLinking.getUuid());
        songLinkDTO.setSongId2(song.getUuid());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SongLinkApiBean songLinkApiBean = new SongLinkApiBean();
                final SongLinkDTO uploaded = songLinkApiBean.uploadSongLink(songLinkDTO);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (uploaded != null && !uploaded.getUuid().trim().isEmpty()) {
                            Toast.makeText(SuggestEditsChooseActivity.this, R.string.successfully_uploaded, Toast.LENGTH_SHORT).show();
                            setResult(1);
                            finish();
                        } else {
                            Toast.makeText(SuggestEditsChooseActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        thread.start();
    }
}
