package projector.model;

public class Book {

    private Chapter[] chapters;
    private String title;

    public Book(Chapter[] chapters) {
        this.chapters = chapters.clone();
    }

    public Book(String title) {
        this.title = title;
    }

    public Book(Chapter[] chapters, String title) {
        this.chapters = chapters.clone();
        this.title = title;
    }

    public Chapter[] getChapters() {
        return chapters == null ? null : chapters.clone();
    }

    public void setChapters(Chapter[] chapters) {
        this.chapters = chapters.clone();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Book copy() {
        Book newBook = new Book(getTitle());
        Chapter[] tmpChapters = new Chapter[getChapters().length];
        for (int iPart = 0; iPart < getChapters().length; ++iPart) {
            tmpChapters[iPart] = new Chapter();
            String[] tmpVerses = new String[getChapters()[iPart].getLength()];
            for (int iVers = 0; iVers < getChapters()[iPart].getLength(); ++iVers) {
                tmpVerses[iVers] = getChapters()[iPart].getVerses()[iVers];
            }
            tmpChapters[iPart].setVerses(tmpVerses);
        }
        newBook.setChapters(tmpChapters);
        return newBook;
    }
}
