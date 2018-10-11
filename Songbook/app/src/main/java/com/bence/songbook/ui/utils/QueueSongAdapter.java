package com.bence.songbook.ui.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.ui.activity.MainActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueueSongAdapter extends ArrayAdapter<QueueSong> {

    private final MainActivity.Listener listener;
    private final Map<QueueSong, Integer> mIdMap = new HashMap<>();
    private final boolean shortCollectionName;

    public QueueSongAdapter(Context context, int textViewResourceId, List<QueueSong> list, MainActivity.Listener listener, boolean shortCollectionName) {
        super(context, textViewResourceId, list);
        this.listener = listener;
        for (int i = 0; i < list.size(); ++i) {
            mIdMap.put(list.get(i), i);
        }
        Memory.getInstance().addOnQueueChangeListener(new Memory.Listener() {
            @Override
            public void onAdd(QueueSong queueSong) {
                mIdMap.put(queueSong, mIdMap.size());
            }

            @Override
            public void onRemove(QueueSong queueSong) {
                mIdMap.remove(queueSong);
            }
        });
        this.shortCollectionName = shortCollectionName;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        Context context = getContext();
        if (null == view) {
            view = LayoutInflater.from(context).inflate(R.layout.list_row, null);
        }
        QueueSong item = getItem(position);
        if (item == null) {
            return view;
        }
        final Song song = item.getSong();
        final LinearLayout row = view.findViewById(R.id.lytPattern);

        TextView ordinalNumberTextView = view.findViewById(R.id.ordinalNumberTextView);
        SongCollection songCollection = song.getSongCollection();
        if (songCollection != null) {
            String collectionName = songCollection.getName();
            if (shortCollectionName) {
                collectionName = songCollection.getShortName();
            }
            String text = collectionName + " " + song.getSongCollectionElement().getOrdinalNumber();
            ordinalNumberTextView.setText(text);
        } else {
            ordinalNumberTextView.setText("");
        }
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        titleTextView.setText(song.getTitle());
        ImageView imageView = view.findViewById(R.id.starImageView);
        imageView.setVisibility(song.isFavourite() ? View.VISIBLE : View.INVISIBLE);

        view.findViewById(R.id.imageViewGrab)
                .setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        listener.onGrab(position, row);
                        return false;
                    }
                });

        return view;
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return -1;
        }
        try {
            QueueSong item = getItem(position);
            if (item != null) {
                return mIdMap.get(item);
            }
        } catch (IndexOutOfBoundsException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    @Override
    public boolean hasStableIds() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

}