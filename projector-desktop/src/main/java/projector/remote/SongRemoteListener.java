package projector.remote;

import javafx.collections.ObservableList;
import projector.controller.song.util.SearchedSong;
import projector.utils.scene.text.MyTextFlow;

import java.util.List;

public interface SongRemoteListener {
    void onSongVerseListViewChanged(List<MyTextFlow> list);

    void onSongListViewChanged(ObservableList<SearchedSong> items);
}
