package projector.application;

import projector.model.Book;
import projector.model.Chapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Reader {

    public static final int numberOfBooks = 66;
    private static String[][][] booksO;
    private static Book[] books;
    private static boolean booksRead = false;

    public static boolean isBooksRead() {
        return booksRead;
    }

    public static void setBooksRead(boolean booksRead) {
        Reader.booksRead = booksRead;
    }

    public static Book[] getBooks(String filePath) {
        if (!booksRead) {
            readBooksB(filePath);
        }
        return books.clone();
    }

    public static Book[] getBooks2(String filePath) {
        if (!booksRead) {
            readBooksB2(filePath);
        }
        return books.clone();
    }

    public static void readBooksB2(String filePath) {
        if (!booksRead) {
            booksRead = true;
            FileInputStream fstream = null;
            try {
                fstream = new FileInputStream(filePath);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));
                br.mark(4);
                if ('\ufeff' != br.read()) {
                    br.reset(); // not the BOM marker
                }
                String strLine;
                String[] a;
                int b;
                books = new Book[numberOfBooks];
                Chapter[][] tmpChapters = new Chapter[numberOfBooks][];

                for (int i = 0; i < numberOfBooks; ++i) {
                    strLine = br.readLine();
                    if (strLine == null) {
                        break;
                    }
                    a = strLine.split(":");
                    b = Integer.parseInt(a[1].substring(1));
                    tmpChapters[i] = new Chapter[b];
                    books[i] = new Book(a[0]);
                    strLine = br.readLine();
                    if (strLine == null) {
                        break;
                    }
                    String[] split = strLine.split(" ");
                    for (int k = 0; k < split.length; k++) {
                        int j = Integer.parseInt(split[k]);
                        tmpChapters[i][k] = new Chapter(j);
                        tmpChapters[i][k].setLength(j);
                    }
                }
                for (int book = 0; book < numberOfBooks; ++book) {
                    for (int part = 0; part < tmpChapters[book].length; ++part) {
                        String[] tmpVerses = new String[tmpChapters[book][part].getLength()];
                        for (int verse = 0; verse < tmpChapters[book][part].getLength(); ++verse) {
                            strLine = br.readLine();
                            String vers = br.readLine();
                            try {
                                tmpVerses[verse] = vers;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        tmpChapters[book][part].setVerses(tmpVerses);
                    }
                }
                for (int i = 0; i < numberOfBooks; ++i) {
                    books[i].setChapters(tmpChapters[i]);
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void readBooksB(String filePath) {
        if (!booksRead) {
            booksRead = true;
            FileInputStream fstream = null;
            try {
                fstream = new FileInputStream(filePath);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));
                br.mark(4);
                if ('\ufeff' != br.read()) {
                    br.reset(); // not the BOM marker
                }
                String strLine;
                String[] a;
                int b;
                books = new Book[numberOfBooks];
                Chapter[][] tmpChapters = new Chapter[numberOfBooks][];

                for (int i = 0; i < numberOfBooks; ++i) {
                    strLine = br.readLine();
                    if (strLine == null) {
                        break;
                    }
                    a = strLine.split(":");
                    b = Integer.parseInt(a[1].substring(1));
                    tmpChapters[i] = new Chapter[b];
                    books[i] = new Book(a[0]);
                    strLine = br.readLine();
                    if (strLine == null) {
                        break;
                    }
                    String[] split = strLine.split(" ");
                    for (int k = 0; k < split.length; k++) {
                        int j = Integer.parseInt(split[k]);
                        tmpChapters[i][k] = new Chapter(j);
                        tmpChapters[i][k].setLength(j);
                    }
                }

                for (int book = 0; book < numberOfBooks; ++book) {
                    for (int part = 0; part < tmpChapters[book].length; ++part) {
                        String[] tmpVerses = new String[tmpChapters[book][part].getLength()];
                        for (int verse = 0; verse < tmpChapters[book][part].getLength(); ++verse) {
                            strLine = br.readLine();
                            tmpVerses[verse] = br.readLine();
                        }
                        tmpChapters[book][part].setVerses(tmpVerses);
                    }
                }
                for (int i = 0; i < numberOfBooks; ++i) {
                    books[i].setChapters(tmpChapters[i]);
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
