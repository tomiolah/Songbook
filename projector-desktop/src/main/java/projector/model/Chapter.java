package projector.model;

public class Chapter {

    private String[] verses;
    private int length;

    public Chapter(String[] verses) {
        this.verses = verses.clone();
    }

    public Chapter() {
    }

    public Chapter(int length) {
        this.length = length;
    }

    public String[] getVerses() {
        return verses == null ? null : verses.clone();
    }

    public void setVerses(String[] verses) {
        this.verses = verses.clone();
        length = verses.length;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}