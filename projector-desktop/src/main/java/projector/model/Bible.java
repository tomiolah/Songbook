package projector.model;

import projector.application.Reader;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Bible {

    private String name;
    private Book[] books;

    public static void main(String[] args) {
        Bible bible = new Bible();
        bible.setBooks(Reader.getBooks2("revkarbiblia.txt"));
        int a = 0;
        int b = 0;
        for (int i = 0; i < bible.getBooks().length; ++i) {
            for (int j = 0; j < bible.getBooks()[i].getChapters().length; ++j) {
                for (int k = 0; k < bible.getBooks()[i].getChapters()[j].getVerses().length; ++k) {
                    String vers = bible.getBooks()[i].getChapters()[j].getVerses()[k];
                    if (vers.contains("[") && !vers.contains("]")) {
                        System.out.println(vers);
                    }
                    for (int p = 0; p < vers.length(); ++p) {
                        if (vers.substring(p, p + 1).contains("[")) {
                            ++a;
                        }
                        if (vers.substring(p, p + 1).contains("]")) {
                            ++b;
                        }
                    }

                    if (a > b || b < a) {
                        System.out.println(vers);
                        a = b = 0;
                    }
                }
            }
        }
        FileOutputStream fstream = null;
        try {
            fstream = new FileOutputStream("hu-ro.txt");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fstream, "UTF-8"));
            for (int i = 0; i < bible.getBooks().length; ++i) {
                for (int j = 0; j < bible.getBooks()[i].getChapters().length; ++j) {
                    for (int k = 0; k < bible.getBooks()[i].getChapters()[j].getVerses().length; ++k) {
                        String vers = bible.getBooks()[i].getChapters()[j].getVerses()[k];
                        bw.write(vers + System.lineSeparator() + System.lineSeparator());
                    }
                }
            }
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Book[] getBooks() {
        return books == null ? null : books.clone();
    }

    public void setBooks(Book[] books) {
        this.books = books.clone();
    }

    public int getBookIndex(String bookName) {
        for (int i = 0; i < books.length; ++i) {
            if (books[i].getTitle().trim().equals(bookName)) {
                return i;
            }
        }
        return -1;
    }
}
