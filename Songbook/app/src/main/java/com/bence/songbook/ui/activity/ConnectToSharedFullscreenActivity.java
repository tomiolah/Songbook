package com.bence.songbook.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.bence.songbook.R;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.network.TCPClient;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ConnectToSharedFullscreenActivity extends AbstractFullscreenActivity {
    private List<String> texts;
    private int textIndex = -1;

    private void setPreviousVerse() {
        if (textIndex > 0) {
            --textIndex;
            setText(texts.get(textIndex));
        }
    }

    private void setNextVerse() {
        if (textIndex + 1 < texts.size()) {
            ++textIndex;
            setText(texts.get(textIndex));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        texts = new ArrayList<>();
        final View mContentView = findViewById(R.id.fullscreen_content);
        // Set up the user interaction to manually show or hide the system UI.
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
        try {
            Intent intent = getIntent();
            String connectToShared = intent.getStringExtra("connectToShared");
            if (connectToShared != null && !connectToShared.isEmpty()) {
                TCPClient.connectToShared(this, connectToShared, new ProjectionTextChangeListener() {
                    @Override
                    public void onSetText(final String text) {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setText(text);
                                    if (textIndex < 0 || !texts.get(textIndex).equals(text)) {
                                        textIndex = texts.size();
                                        texts.add(text);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            setText(getString(R.string.connection_successfully_wait_));
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            if (message != null) {
                Log.e(ConnectToSharedFullscreenActivity.class.getSimpleName(), message);
            }
        }
    }
}
