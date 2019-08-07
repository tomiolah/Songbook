package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.ui.utils.OnSwipeTouchListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AbstractFullscreenActivity {

    private int verseIndex;
    private List<SongVerse> verseList;
    private Song song;
    private long startTime;
    private long duration = 0;
    private SongRepositoryImpl songRepository;
    private List<ProjectionTextChangeListener> projectionTextChangeListeners;
    private boolean sharedOnNetwork = false;
    private Date lastDatePressedAtEnd = null;
    private boolean show_title_switch;
    private Memory memory = Memory.getInstance();
    private boolean blank_switch;
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (memory.isShareOnNetwork()) {
                sharedOnNetwork = true;
                projectionTextChangeListeners = memory.getProjectionTextChangeListeners();
            }
            song = memory.getPassingSong();
            Intent intent = getIntent();
            verseList = new ArrayList<>(song.getVerses().size());
            final List<SongVerse> verses = song.getVerses();
            SongVerse chorus = null;
            int size = verses.size();
            verseIndex = intent.getIntExtra("verseIndex", 0);
            for (int i = 0; i < size; ++i) {
                SongVerse songVerse = verses.get(i);
                verseList.add(songVerse);
                if (songVerse.isChorus()) {
                    chorus = songVerse;
                } else if (chorus != null) {
                    if (i + 1 < size) {
                        if (!verses.get(i + 1).isChorus()) {
                            if (verseList.size() <= verseIndex) {
                                ++verseIndex;
                            }
                            verseList.add(chorus);
                        }
                    } else {
                        if (verseList.size() <= verseIndex) {
                            ++verseIndex;
                        }
                        verseList.add(chorus);
                    }
                }
            }

            mContentView = findViewById(R.id.fullscreen_content);
            mContentView.setOnTouchListener(new OnSwipeTouchListener(this) {

                public void onSwipeTop() {
                    if (memory.getQueue().size() > 0) {
                        setNextInQueue(AnimationUtils.loadAnimation(FullscreenActivity.this, R.anim.slide_from_bottom));
                    }
                }

                public void onSwipeLeft() {
                    setNextVerse();
                }

                public void onSwipeRight() {
                    setPreviousVerse();
                }

                public void onSwipeBottom() {
                    if (memory.getQueue().size() > 0) {
                        setPrevInQueue();
                    }
                }

                @SuppressLint("ClickableViewAccessibility")
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }

                @Override
                public void performTouchLeftRight(MotionEvent event) {
                    if (event.getX() < mContentView.getWidth() / 2) {
                        setPreviousVerse();
                    } else {
                        setNextVerse();
                    }
                }
            });

            SharedPreferences sharedPreferences = settingTitleSlide();
            blank_switch = sharedPreferences.getBoolean("blank_switch", false);
            if (blank_switch) {
                addBlankSlide();
            } else {
                List<QueueSong> queue = memory.getQueue();
                if (queue.size() > 1) {
                    addBlankSlide();
                }
            }
            if (verseIndex < 0) {
                verseIndex = 0;
            }
            setText(verseList.get(verseIndex).getText());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    songRepository = new SongRepositoryImpl(getApplicationContext());
                }
            });
            thread.start();
            super.setContext(this);
        } catch (Exception e) {
            Log.e(FullscreenActivity.class.getSimpleName(), e.getMessage());
        }
    }

    private void addBlankSlide() {
        SongVerse songVerse = new SongVerse();
        songVerse.setText("");
        verseList.add(songVerse);
    }

    @NonNull
    private SharedPreferences settingTitleSlide() {
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
            ++verseIndex;
        }
        return sharedPreferences;
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
        super.hide();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateSongAccessedTime();
    }

    private void updateSongAccessedTime() {
        if (songRepository != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Song song = songRepository.findOne(FullscreenActivity.this.song.getId());
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
            textView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_from_left));
        }
    }

    private void setNextVerse() {
        if (verseIndex + 1 < verseList.size()) {
            ++verseIndex;
            setText(verseList.get(verseIndex).getText());
            textView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_from_right));
            return;
        }
        int queueIndex = memory.getQueueIndex();
        if (queueIndex >= 0 && memory.getQueue().size() > 1) {
            setNextInQueue(AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom));
            return;
        }
        checkPressTwice();
    }

    private void setNextInQueue(Animation animation) {
        int queueIndex = memory.getQueueIndex();
        updateSongAccessedTime();
        startTime = new Date().getTime();
        duration = 0;
        List<QueueSong> queue = memory.getQueue();
        if (queue.size() <= queueIndex) {
            return;
        }
        song = queue.get(queueIndex).getSong();
        setVerseSlides();
        settingTitleSlide();
        if (queueIndex + 1 < queue.size()) {
            memory.setQueueIndex(queueIndex + 1, this);
            addBlankSlide();
        } else if (queueIndex > 0) {
            memory.setQueueIndex(0, this);
            addBlankSlide();
        } else {
            if (blank_switch) {
                addBlankSlide();
            }
        }
        verseIndex = 0;
        setText(verseList.get(verseIndex).getText());
        textView.startAnimation(animation);
    }

    private void setPrevInQueue() {
        int queueIndex = memory.getQueueIndex();
        if (queueIndex - 2 >= 0) {
            memory.setQueueIndex(queueIndex - 2, this);
        } else {
            int size = memory.getQueue().size();
            if (queueIndex == 0 && size > 1) {
                memory.setQueueIndex(size - 2, this);
            } else {
                memory.setQueueIndex(size - 1, this);
            }
        }
        setNextInQueue(AnimationUtils.loadAnimation(this, R.anim.slide_from_top));
    }

    private void setVerseSlides() {
        verseList.clear();
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
    }

    private void checkPressTwice() {
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
                try {
                    setText(verseList.get(verseIndex).getText());
                } catch (IndexOutOfBoundsException e) {
                    verseIndex = 0;
                }
                lastDatePressedAtEnd = null;
                return;
            }
        }
        lastDatePressedAtEnd = now;
    }

    @Override
    void setText(String text) {
        super.setText(text);
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
