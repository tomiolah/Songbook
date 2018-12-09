package projector.remote;

import projector.utils.scene.text.MyTextFlow;

import java.util.List;

public interface SongRemoteListener {
    void onSongListViewChanged(List<MyTextFlow> list);
}
