package dbservices.dataset;

public class BooksDataSet {

    private long id;
    private String title;
    private String authors;
    private String translators;
    private String publisher;
    private String ISBN;
    private String annotation;

    public BooksDataSet(long id, String title, String authors, String translators, String publisher, String ISBN, String annotation) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.translators = translators;
        this.publisher = publisher;
        this.ISBN = ISBN;
        this.annotation = annotation;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "BooksDataSet{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authors='" + authors + '\'' +
                ", translators='" + translators + '\'' +
                ", publisher='" + publisher + '\'' +
                ", ISBN='" + ISBN + '\'' +
                ", annotation='" + annotation + '\'' +
                '}';
    }
}
