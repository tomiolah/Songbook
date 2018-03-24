package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.network.TCPServer;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private int verseIndex;
    private TextView textView;
    private List<SongVerse> verseList;
    private Song song;
    private long startTime;
    private long duration = 0;
    private SongRepositoryImpl songRepository;
    private List<ProjectionTextChangeListener> projectionTextChangeListeners;
    private boolean sharedOnNetwork = false;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Memory memory = Memory.getInstance();
            if (memory.isShareOnNetwork()) {
                sharedOnNetwork = memory.isSharedOnNetwork();
                if (!sharedOnNetwork) {
                    try {
                        projectionTextChangeListeners = new ArrayList<>();
                        TCPServer.startShareNetwork(projectionTextChangeListeners);
                        sharedOnNetwork = true;
                        memory.setSharedOnNetwork();
                        memory.setProjectionTextChangeListeners(projectionTextChangeListeners);
                    } catch (Exception ignored) {
                    }
                } else {
                    projectionTextChangeListeners = memory.getProjectionTextChangeListeners();
                }
            }
            Intent intent = getIntent();
            song = (Song) intent.getSerializableExtra("Song");
            verseList = new ArrayList<>(song.getVerses().size());
            final List<SongVerse> verses = song.getVerses();
            SongVerse chorus = null;
            int size = verses.size();
            for (int i = 0; i < size; ++i) {
                SongVerse songVerse = verses.get(i);
                verseList.add(songVerse);
                if (songVerse.isChorus()) {
                    chorus = songVerse;
                } else if (chorus != null) {
                    if (i + 1 < size) {
                        if (!verses.get(i + 1).isChorus()) {
                            verseList.add(chorus);
                        }
                    } else {
                        verseList.add(chorus);
                    }
                }
            }
            verseIndex = intent.getIntExtra("verseIndex", 0);

            setContentView(R.layout.activity_fullscreen);

            mControlsView = findViewById(R.id.fullscreen_content_controls);
            mContentView = findViewById(R.id.fullscreen_content);

            // Set up the user interaction to manually show or hide the system UI.
            mContentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (motionEvent.getX() < textView.getWidth() / 2) {
                            setPreviousVerse();
                        } else {
                            setNextVerse();
                        }
                    }
                    return true;
                }
            });

            // Upon interacting with UI controls, delay any scheduled hide()
            // operations to prevent the jarring behavior of controls going away
            // while interacting with the UI.

            textView = findViewById(R.id.fullscreen_content);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int max_text_size = sharedPreferences.getInt("max_text_size", -1);
            if (max_text_size > 0) {
                textView.setTextSize(max_text_size);
            }
            boolean light_theme = sharedPreferences.getBoolean("light_theme_switch", false);
            if (light_theme) {
                textView.setBackgroundResource(R.color.white);
                textView.setTextColor(getResources().getColor(R.color.black));
            } else {
                textView.setBackgroundResource(R.color.black);
                textView.setTextColor(getResources().getColor(R.color.white));
            }
            setText(verseList.get(verseIndex).getText());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    songRepository = new SongRepositoryImpl(getApplicationContext());
                }
            });
            thread.start();
        } catch (Exception e) {
            Log.e(FullscreenActivity.class.getSimpleName(), e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        long endTime = new Date().getTime();
        duration += endTime - startTime;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime = new Date().getTime();
        hide();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (songRepository != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Song song = songRepository.findOne(FullscreenActivity.this.song.getId());
                    Date date = new Date();
                    long endTime = date.getTime();
                    duration += endTime - startTime;
                    song.setLastAccessed(date);
                    Long accessedTimes = song.getAccessedTimes();
                    song.setAccessedTimes(accessedTimes + 1);
                    song.setAccessedTimeAverage((accessedTimes * song.getAccessedTimeAverage() + duration) / song.getAccessedTimes());
                    songRepository.save(song);
                }
            });
            thread.start();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                setPreviousVerse();
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                setNextVerse();
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    private void setPreviousVerse() {
        if (verseIndex > 0) {
            --verseIndex;
            setText(verseList.get(verseIndex).getText());
        }
    }

    private void setNextVerse() {
        if (verseIndex + 1 < verseList.size()) {
            ++verseIndex;
            setText(verseList.get(verseIndex).getText());
        }
    }

    private void setText(String text) {
        textView.setText(text);
        sendTextToListeners(text);
    }

    private synchronized void sendTextToListeners(final String text) {
        if (sharedOnNetwork) {
            for (int i = 0; i < projectionTextChangeListeners.size(); ++i) {
                projectionTextChangeListeners.get(i).onSetText(text);
            }
        }
    }
}
