package projector.service;

import projector.service.impl.BibleServiceImpl;
import projector.service.impl.InformationServiceImpl;
import projector.service.impl.LanguageServiceImpl;
import projector.service.impl.SongBookServiceImpl;
import projector.service.impl.SongCollectionServiceImpl;
import projector.service.impl.SongServiceImpl;
import projector.service.impl.SongVerseServiceImpl;
import projector.service.impl.VerseIndexServiceImpl;

public class ServiceManager {

    public static SongBookService getSongBookService() {
        return new SongBookServiceImpl();
    }

    public static SongService getSongService() {
        return new SongServiceImpl();
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
}
