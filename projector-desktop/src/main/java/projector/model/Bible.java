package projector.model;

public class Bible {

    private String name;
    private Book[] books;
    private String path;
    private int usage = 0;

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

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
