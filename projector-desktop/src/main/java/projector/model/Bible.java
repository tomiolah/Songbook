package projector.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bible extends BaseEntity {

    @DatabaseField
    private String name;
    @DatabaseField
    private String shortName;
    @ForeignCollectionField
    private ForeignCollection<Book> bookForeignCollection;
    private List<Book> books;
    @DatabaseField(foreign = true, index = true)
    private Language language;
    private String path;

    @DatabaseField
    private int usage = 0;
    @DatabaseField
    private Date createdDate;
    @DatabaseField
    private Date modifiedDate;
    @DatabaseField
    private int parallelNumber = 0;
    private Color color;
    @DatabaseField
    private Double red;
    @DatabaseField
    private Double green;
    @DatabaseField
    private Double blue;
    @DatabaseField
    private Double opacity;
    @DatabaseField
    private Integer showAbbreviation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBookIndex(String bookName) {
        for (int i = 0; i < getBooks().size(); ++i) {
            if (getBooks().get(i).getTitle().trim().equals(bookName)) {
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

    public List<Book> getBooks() {
        if (books == null && bookForeignCollection != null) {
            books = new ArrayList<>(bookForeignCollection.size());
            books.addAll(bookForeignCollection);
        }
        return books;
    }

    public void setBooks(List<Book> books) {
        for (Book book : books) {
            book.setBible(this);
        }
        this.books = books;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        if (shortName == null) {
            return name;
        }
        return shortName;
    }

    public int getParallelNumber() {
        return parallelNumber;
    }

    public void setParallelNumber(int parallelNumber) {
        this.parallelNumber = parallelNumber;
    }

    public Color getColor() {
        if (color == null && red != null) {
            color = Color.color(red, green, blue, opacity);
        }
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
        opacity = color.getOpacity();
    }

    public boolean isShowAbbreviation() {
        return showAbbreviation == null || showAbbreviation < 0;
    }

    public void setShowAbbreviation(boolean showAbbreviation) {
        if (showAbbreviation) {
            this.showAbbreviation = 1;
        } else {
            this.showAbbreviation = -1;
        }
    }
}
