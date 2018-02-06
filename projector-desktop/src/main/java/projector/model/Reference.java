package projector.model;

import java.util.LinkedList;
import java.util.List;

public class Reference {

    private String reference;
    private List<ReferenceBook> bookList;
    private Bible bible;

    public Reference() {
        bookList = new LinkedList<>();
    }

    public Reference(String reference) {
        super();
        this.reference = reference;
    }

    public String getReference() {
        reference = "";
        for (ReferenceBook i : bookList) {
            if (!reference.equals("")) {
                reference += "\n";
            }
            reference += bible.getBooks()[i.getBookNumber()].getTitle().trim();
            ReferenceChapter c = i.getChapters().get(0);
            reference += " " + c.getChapterNumber() + ":";
            reference += c.getVerses().get(0);
            int k;
            for (k = 1; k < c.getVerses().size() - 1; ++k) {
                if (c.getVerses().get(k + 1) - 1 != c.getVerses().get(k)) {
                    if (c.getVerses().get(k - 1) + 1 == c.getVerses().get(k)) {
                        reference += "-";
                    } else {
                        reference += ", ";
                    }
                    reference += c.getVerses().get(k);
                } else if (c.getVerses().get(k - 1) + 1 != c.getVerses().get(k)) {
                    reference += ", ";
                    reference += c.getVerses().get(k);
                }
            }
            if (k < c.getVerses().size()) {
                if (c.getVerses().get(k - 1) + 1 == c.getVerses().get(k)) {
                    reference += "-";
                } else {
                    reference += ", ";
                }
                reference += c.getVerses().get(k);
            }
            for (int j = 1; j < i.getChapters().size(); ++j) {
                ReferenceChapter c1 = i.getChapters().get(j);
                reference += "\n" + c1.getChapterNumber() + ":";
                reference += c1.getVerses().get(0);
                for (k = 1; k < c1.getVerses().size() - 1; ++k) {
                    if (c1.getVerses().get(k + 1) - 1 != c1.getVerses().get(k)) {
                        if (c1.getVerses().get(k - 1) + 1 == c1.getVerses().get(k)) {
                            reference += "-";
                        } else {
                            reference += ", ";
                        }
                        reference += c1.getVerses().get(k);
                    } else if (c1.getVerses().get(k - 1) + 1 != c1.getVerses().get(k)) {
                        reference += ", ";
                        reference += c1.getVerses().get(k);
                    }
                }
                if (k < c1.getVerses().size()) {
                    if (c1.getVerses().get(k - 1) + 1 == c1.getVerses().get(k)) {
                        reference += "-";
                    } else {
                        reference += ", ";
                    }
                    reference += c1.getVerses().get(k);
                }
            }
        }
        return reference;
    }

    public void setBible(Bible bible) {
        this.bible = bible;
    }

    public void addVers(int book, int chapter, int vers) {
        for (ReferenceBook i : bookList) {
            if (i.getBookNumber() == book) {
                i.addVers(chapter, vers);
                return;
            }
        }
        bookList.add(new ReferenceBook(book));
        addVers(book, chapter, vers);
    }

    public void addVers(String reference) {
        String[] split = reference.split(" ");
        int bookIndex = Integer.parseInt(split[0]) - 1;
        int partIndex = Integer.parseInt(split[1]);
        addingVerses(split[2], bookIndex, partIndex);
    }

    private void addingVerses(String s, int bookIndex, int partIndex) {
        if (s.contains(",")) {
            String[] split = s.split(",");
            for (String withoutComma : split) {
                addingVersesWithoutComma(withoutComma, bookIndex, partIndex);
            }
        } else {
            addingVersesWithoutComma(s, bookIndex, partIndex);
        }
    }

    private void addingVersesWithoutComma(String withoutComma, int bookIndex, int partIndex) {
        if (!withoutComma.contains("-")) {
            int verseIndex = Integer.parseInt(withoutComma);
            addVers(bookIndex, partIndex, verseIndex);
        } else {
            String[] split2 = withoutComma.split("-");
            int fromVerse = Integer.parseInt(split2[0]);
            int toVerse = Integer.parseInt(split2[1]);
            for (int i = fromVerse; i <= toVerse; ++i) {
                addVers(bookIndex, partIndex, i);
            }
        }
    }

    public List<ReferenceBook> getBookList() {
        return bookList;
    }

    public void removeVers(int book, int chapter, int vers) {
        for (ReferenceBook i : bookList) {
            if (i.getBookNumber() == book) {
                i.removeVers(chapter, vers);
                if (i.isEmpty()) {
                    bookList.remove(i);
                }
                return;
            }
        }
    }

    public String getReference(Bible bible) {
        this.bible = bible;
        return getReference();
    }

    public void clear() {
        for (ReferenceBook referenceBook : bookList) {
            referenceBook.clear();
        }
        bookList.clear();
    }
}
