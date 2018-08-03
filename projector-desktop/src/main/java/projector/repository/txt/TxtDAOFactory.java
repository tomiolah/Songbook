package projector.repository.txt;

import projector.repository.BibleRepository;
import projector.repository.DAOFactory;
import projector.repository.InformationDAO;
import projector.repository.LanguageRepository;
import projector.repository.SongBookDAO;
import projector.repository.SongCollectionElementRepository;
import projector.repository.SongCollectionRepository;
import projector.repository.SongDAO;
import projector.repository.SongVerseDAO;
import projector.repository.VerseIndexRepository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class TxtDAOFactory extends DAOFactory {
    @Override
    public SongBookDAO getSongBookDAO() {
        return new SongBookDAOImpl();
    }

    @Override
    public SongDAO getSongDAO() {
        return new SongDAOImpl();
    }

    @Override
    public InformationDAO getInformationDAO() {
        throw new NotImplementedException();
    }

    @Override
    public SongVerseDAO getSongVerseDAO() {
        throw new NotImplementedException();
    }

    @Override
    public LanguageRepository getLanguageDAO() {
        return null;
    }

    @Override
    public SongCollectionRepository getSongCollectionDAO() {
        return null;
    }

    @Override
    public SongCollectionElementRepository getSongCollectionElementDAO() {
        return null;
    }

    @Override
    public BibleRepository getBibleDAO() {
        return null;
    }

    @Override
    public VerseIndexRepository getVerseIndexDAO() {
        return null;
    }
}
