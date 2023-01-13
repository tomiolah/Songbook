package projector.service;

import projector.service.impl.BibleServiceImpl;
import projector.service.impl.CountdownTimeServiceImpl;
import projector.service.impl.InformationServiceImpl;
import projector.service.impl.LanguageServiceImpl;
import projector.service.impl.SongCollectionElementServiceImpl;
import projector.service.impl.SongCollectionServiceImpl;
import projector.service.impl.SongServiceImpl;
import projector.service.impl.SongVerseServiceImpl;
import projector.service.impl.VerseIndexServiceImpl;
import projector.service.sqlite.BooksService;
import projector.service.sqlite.BooksServiceImpl;
import projector.service.sqlite.InfoService;
import projector.service.sqlite.InfoServiceImpl;
import projector.service.sqlite.VersesService;
import projector.service.sqlite.VersesServiceImpl;

public class ServiceManager {

    private static SongServiceImpl songService;

    public static SongService getSongService() {
        if (songService == null) {
            songService = new SongServiceImpl();
        }
        return songService;
    }

    public static SongVerseService getSongVerseService() {
        return new SongVerseServiceImpl();
    }

    public static InformationService getInformationService() {
        return new InformationServiceImpl();
    }

    public static LanguageService getLanguageService() {
        return new LanguageServiceImpl();
    }

    public static SongCollectionService getSongCollectionService() {
        return new SongCollectionServiceImpl();
    }

    public static BibleService getBibleService() {
        return new BibleServiceImpl();
    }

    public static VerseIndexService getVerseIndexService() {
        return new VerseIndexServiceImpl();
    }

    public static SongCollectionElementService getSongCollectionElementService() {
        return new SongCollectionElementServiceImpl();
    }

    public static BooksService getBooksService() {
        return new BooksServiceImpl();
    }

    public static VersesService getVersesService() {
        return new VersesServiceImpl();
    }

    public static InfoService getInfoService() {
        return new InfoServiceImpl();
    }

    public static CountdownTimeService getCountdownTimeService() {
        return new CountdownTimeServiceImpl();
    }
}
