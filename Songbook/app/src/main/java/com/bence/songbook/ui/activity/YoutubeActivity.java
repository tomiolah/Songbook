package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bence.songbook.R;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.utils.Config;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class YoutubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final int RECOVERY_REQUEST = 1;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
        }
    };
    TextView textView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            textView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private YouTubePlayerView youTubeView;
    private Song song;
    private int verseIndex;
    private List<SongVerse> verseList;
    private long startTime;
    private long duration = 0;
    private SongRepositoryImpl songRepository;
    private Date lastDatePressedAtEnd = null;
    private boolean show_title_switch;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_activity);

        Intent intent = getIntent();
        song = (Song) intent.getSerializableExtra("song");
        youTubeView = findViewById(R.id.youtube_view);
        youTubeView.initialize(Config.YOUTUBE_API_KEY, this);
        try {
            textView = findViewById(R.id.fullscreen_content);
            textView.setSingleLine(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);
            }
            // Set up the user interaction to manually show or hide the system UI.
            // Upon interacting with UI controls, delay any scheduled hide()
            // operations to prevent the jarring behavior of controls going away
            // while interacting with the UI.
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
        } catch (Exception e) {
            Log.e(AbstractFullscreenActivity.class.getSimpleName(), e.getMessage());
        }
        try {
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

            final View mContentView = findViewById(R.id.fullscreen_content);
            mContentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (motionEvent.getX() < mContentView.getWidth() / 2) {
                            setPreviousVerse();
                        } else {
                            setNextVerse();
                        }
                        view.performClick();
                    }
                    return true;
                }
            });

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            show_title_switch = sharedPreferences.getBoolean("show_title_switch", false);
            if (show_title_switch) {
                SongVerse songVerse = new SongVerse();
                String title = "";
                SongCollection songCollection = song.getSongCollection();
                if (songCollection != null) {
                    String name = songCollection.getName();
                    SongCollectionElement songCollectionElement = song.getSongCollectionElement();
                    if (songCollectionElement != null) {
                        String ordinalNumber = songCollectionElement.getOrdinalNumber().trim();
                        if (!ordinalNumber.isEmpty()) {
                            name += " " + ordinalNumber;
                        }
                    }
                    title = name + "\n";
                }
                title += song.getTitle();
                songVerse.setText(title);
                verseList.add(0, songVerse);
            }
            boolean blank_switch = sharedPreferences.getBoolean("blank_switch", false);
            if (blank_switch) {
                SongVerse songVerse = new SongVerse();
                songVerse.setText("");
                verseList.add(songVerse);
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
    public void onInitializationSuccess(Provider provider, final YouTubePlayer player, boolean wasRestored) {
        player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(String s) {
            }

            @Override
            public void onAdStarted() {
            }

            @Override
            public void onVideoStarted() {
            }

            @Override
            public void onVideoEnded() {
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
                if (errorReason == YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY) {
                    hide();
                    player.play();
//                } else {
//                    System.out.println(errorReason);
                }
            }
        });
        if (!wasRestored) {
            player.cueVideo(song.getYoutubeUrl());
        }
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format("Error initializing YouTube player: %s", errorReason.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(Config.YOUTUBE_API_KEY, this);
        }
    }

    protected Provider getYouTubePlayerProvider() {
        return youTubeView;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide();
    }

    void hide() {
        // Hide UI first

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

    void setText(String text) {
        String s = text.replaceAll("<color=\"0x(.{0,6})..\">", "<font color='0x$1'>")
                .replaceAll("</color>", "</font>")
                .replaceAll("\\[", "<i>")
                .replaceAll("]", "</i>")
                .replaceAll("\n", "<br>");
        textView.setText(Html.fromHtml(s), TextView.BufferType.SPANNABLE);
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
                    try {
                        Song song = songRepository.findOne(YoutubeActivity.this.song.getId());
                        Date date = new Date();
                        long endTime = date.getTime();
                        duration += endTime - startTime;
                        song.setLastAccessed(date);
                        Long accessedTimes = song.getAccessedTimes();
                        song.setAccessedTimes(accessedTimes + 1);
                        song.setAccessedTimeAverage((accessedTimes * song.getAccessedTimeAverage() + duration) / song.getAccessedTimes());
                        songRepository.save(song);
                    } catch (Exception ignored) {
                    }
                }
            });
            thread.start();
        }
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
        } else {
            Date now = new Date();
            int interval = 777;
            if (lastDatePressedAtEnd != null) {
                if (now.getTime() - lastDatePressedAtEnd.getTime() >= interval) {
                    Toast.makeText(this, R.string.press_twice, Toast.LENGTH_SHORT).show();
                } else {
                    if (show_title_switch) {
                        verseIndex = 1;
                    } else {
                        verseIndex = 0;
                    }
                    setText(verseList.get(verseIndex).getText());
                    lastDatePressedAtEnd = null;
                    return;
                }
            }
            lastDatePressedAtEnd = now;
        }
    }
}
