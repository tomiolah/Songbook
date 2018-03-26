package com.bence.songbook.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bence.songbook.R;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.network.TCPClient;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ConnectToSharedFullscreenActivity extends AbstractFullscreenActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
