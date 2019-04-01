package projector.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import projector.Main;
import projector.api.BibleApiBean;
import projector.application.Settings;
import projector.controller.IndicesForBibleController;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.Language;
import projector.model.VerseIndex;
import projector.service.ServiceManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BibleImport {

    private static Settings settings = Settings.getInstance();

    public static void bibleImport() {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream("books.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            String readLine = br.readLine();
            while (readLine != null) {
                s.append(readLine);
                readLine = br.readLine();
            }
            Gson gson = new GsonBuilder().serializeNulls().create();
            ArrayList<Book> bookArrayList;
            Type listType = new TypeToken<ArrayList<Book>>() {
            }.getType();
            bookArrayList = gson.fromJson(s.toString(), listType);
            System.out.println(bookArrayList.size());

            Bible bible = new Bible();
            bible.setBooks(bookArrayList);
            List<Language> languages = ServiceManager.getLanguageService().findAll();
            bible.setLanguage(languages.get(3));
            bible.setCreatedDate(new Date());
            bible.setModifiedDate(bible.getCreatedDate());
            bible.setName("Hoffnung für Alle, 2015");
            bible.setShortName("HFA");

            verseImport(bible);
            ServiceManager.getBibleService().create(bible);
            //createIndices(bible);
            //setIndicesForBible(bible);
        } catch (IOException ignored) {
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void verseImport(Bible bible) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream("verses.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            String readLine = br.readLine();
            while (readLine != null) {
                s.append(readLine);
                readLine = br.readLine();
            }
            Gson gson = new GsonBuilder().serializeNulls().create();
            ArrayList<Verse> verseArrayList;
            Type listType = new TypeToken<ArrayList<Verse>>() {
            }.getType();
            verseArrayList = gson.fromJson(s.toString(), listType);
            System.out.println(verseArrayList.size());
            String previousBookNumber = "";
            String previousChapter = "";
            List<Book> books = bible.getBooks();
            int bookI = -1;
            Book book = null;
            Chapter chapter = null;
            for (Verse verse : verseArrayList) {
                if (!verse.getBookNumber().equals(previousBookNumber)) {
                    ++bookI;
                    book = books.get(bookI);
                    previousChapter = "";
                }
                if (!verse.getChapter().equals(previousChapter)) {
                    chapter = new Chapter();
                    chapter.setNumber((short) Integer.parseInt(verse.getChapter()));
                    chapter.setBook(book);
                    List<Chapter> chapters = book.getChapters();
                    if (chapters == null) {
                        chapters = new ArrayList<>();
                        book.setChapters(chapters);
                    }
                    chapters.add(chapter);
                }
                previousBookNumber = verse.getBookNumber();
                previousChapter = verse.getChapter();
                BibleVerse bibleVerse = new BibleVerse();
                bibleVerse.setChapter(chapter);
                bibleVerse.setText(verse.getText());
                bibleVerse.setNumber((short) Integer.parseInt(verse.getVerse()));
                List<BibleVerse> verses = chapter.getVerses();
                if (verses == null) {
                    verses = new ArrayList<>(50);
                    chapter.setVerses(verses);
                }
                verses.add(bibleVerse);
            }
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static void uploadBible(Bible bible) {
        BibleApiBean bibleApiBean = new BibleApiBean();
        Bible uploadedBible = bibleApiBean.uploadBible(bible);
        System.out.println("accomplished");
    }

    @SuppressWarnings("unused")
    private static void createIndices(Bible bible) {
        int k = 1;
        for (Book book : bible.getBooks()) {
            short chapterNr = 1;
            for (Chapter chapter : book.getChapters()) {
                chapter.setNumber(chapterNr++);
                short verseNr = 1;
                for (BibleVerse bibleVerse : chapter.getVerses()) {
                    bibleVerse.setNumber(verseNr++);
                    ArrayList<VerseIndex> verseIndices = new ArrayList<>();
                    VerseIndex verseIndex = new VerseIndex();
                    verseIndex.setIndexNumber((long) (k++ * 1000));
                    verseIndices.add(verseIndex);
                    bibleVerse.setVerseIndices(verseIndices);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static void setIndicesForBible(Bible otherBible) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/view/IndicesForBibleView.fxml"));
            loader.setResources(settings.getResourceBundle());
            Pane root = loader.load();
            IndicesForBibleController controller = loader.getController();
            List<Bible> bibles = ServiceManager.getBibleService().findAll();
            controller.setLeftBible(bibles.get(0));
            controller.setOtherBible(otherBible);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(BibleImport.class.getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Indices");
            stage.show();
        } catch (Exception ignored) {
        }
    }

    private class Verse {
        private String bookNumber;
        private String chapter;
        private String text;
        private String verse;

        String getBookNumber() {
            return bookNumber;
        }

        public void setBookNumber(String bookNumber) {
            this.bookNumber = bookNumber;
        }

        public String getChapter() {
            return chapter;
        }

        public void setChapter(String chapter) {
            this.chapter = chapter;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getVerse() {
            return verse;
        }

        public void setVerse(String verse) {
            this.verse = verse;
        }
    }
}
