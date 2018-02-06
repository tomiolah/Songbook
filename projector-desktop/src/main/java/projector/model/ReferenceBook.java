package projector.model;

import projector.application.Settings;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ReferenceBook {

    private List<ReferenceChapter> chapters;
    private int bookNumber;

    ReferenceBook(int bookNumber) {
        this.bookNumber = bookNumber;
        chapters = new LinkedList<>();
    }

    private void addChapter(int index) {
        chapters.add(new ReferenceChapter(index));
    }

    public List<ReferenceChapter> getChapters() {
        if (Settings.getInstance().isReferenceChapterSorting()) {
            chapters.sort(Comparator.comparingInt(ReferenceChapter::getChapterNumber));
        }
        if (Settings.getInstance().isReferenceVerseSorting()) {
            for (ReferenceChapter i : chapters) {
                i.sort();
            }
        }
        return chapters;
    }

    void addVers(int chapter, int vers) {
        for (ReferenceChapter chapter1 : chapters) {
            if (chapter1.getChapterNumber() == chapter) {
                chapter1.addVers(vers);
                return;
            }
        }
        addChapter(chapter);
        addVers(chapter, vers);
    }

    public int getBookNumber() {
        return bookNumber;
    }

    void removeVers(int chapter, int vers) {
        for (int i = 0; i < chapters.size(); ++i) {
            if (chapters.get(i).getChapterNumber() == chapter) {
                chapters.get(i).removeVers(vers);
                if (chapters.get(i).isEmpty()) {
                    chapters.remove(i);
                }
                return;
            }
        }
    }

    public boolean isEmpty() {
        return chapters.isEmpty();
    }

    public void clear() {
        for (ReferenceChapter referenceChapter : chapters) {
            referenceChapter.clear();
        }
        chapters.clear();
    }
}
