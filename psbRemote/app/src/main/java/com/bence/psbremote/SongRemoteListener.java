package com.bence.psbremote;

import java.util.List;

public interface SongRemoteListener {
    void onSongListViewChanged(List<String> list);
}
