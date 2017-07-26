package dbservices.dataset;

import java.util.ArrayList;
import java.util.Comparator;

public class BooksDataSet {

    private long id;
    private String title;
    private ArrayList<String> authors;
    private String translators;
    private String publisher;
    private String ISBN;
    private String annotation;

    public BooksDataSet(long id, String title, ArrayList<String> authors, String translators, String publisher, String ISBN, String annotation) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.translators = translators;
        this.publisher = publisher;
        this.ISBN = ISBN;
        this.annotation = annotation;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getAuthors() {
        return authors;
    }

    @Override
    public String toString() {
        return "\n{ Book id = " + id +
                " title='" + title + '\'' +
                ", authors='" + authors + '\'' +
                ", translators='" + translators + '\'' +
                ", publisher='" + publisher + '\'' +
                ", ISBN='" + ISBN + '\'' +
                ", annotation='" + annotation + '\'' +
                '}';
    }


}
