package parser;

import dbservices.DBException;
import dbservices.DBService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class BookParser implements Runnable {

    private Document book;
    private DBService dbService;

    BookParser(Document book, DBService dbService) {
        this.book = book;
        this.dbService = dbService;
    }

    @Override
    public void run() {
        Element product = getProduct(book);
        Elements redaction = getRedaction(product);

        String title = getTitle(product);

        //TODO: should be an ArrayList to send to DB
//        ArrayList authors = getAuthors(redaction);
        String authors = getAuthors(redaction).toString().replaceAll("\\[|\\]", "").replaceAll(", ","\t");

        String translators = getTranslators(redaction);
        String publisher = getPublisher(product);
        String ISBN =  getIsbn(book);
        String annotation = getAnnotation(book);

        try {
            dbService.addBook(title, authors,translators, publisher, ISBN, annotation);
        } catch (DBException e) {
            e.printStackTrace();
        }

    }

    private Element getProduct(Document bookPage) {
        return bookPage.select("div#product").first();
    }

    private Elements getRedaction(Element product) {
        return product.select("div.authors");
    }

    private String getTitle(Element product) {
        Element prodTitle = product.select(".prodtitle").first();
        String rusTitle = prodTitle.select("h1").first().text();
        System.out.println(rusTitle.substring(rusTitle.lastIndexOf(":") + 2));
//        String engTitle = (prodTitle.select("h2").first() != null) ? prodTitle.select("h2").first().text() : "Null";
        return rusTitle.substring(rusTitle.lastIndexOf(":") + 2);
    }

    private ArrayList<String> getAuthors(Elements redaction) {
        ArrayList<String> list = new ArrayList<>();
        Elements authors = redaction.select("div:contains(Автор)"); // authors
        authors = authors.select("a");
        for (Element writer : authors) {
            list.add(writer.select("a").text());
//            System.out.printf("Author: %s%n", writer.select("a").text());
        }
        return list;
    }

    private String getTranslators(Elements redaction) {
        Elements translators = redaction.select("div:contains(Переводчик)");
//        System.out.printf("Translator: %s%n", translators.select("a").text());
        return translators.select("a").text();
    }

    private String getPublisher(Element product) {
        Elements publisher = product.select(".publisher");
//        System.out.printf("Publisher: %s%n", publisher.select("a").text());
        return publisher.select("a").text();
    }

    private String getIsbn(Document bookPage) {
        Element isbn = bookPage.select("div.isbn").first();
//        System.out.println(isbn.text());
        return isbn.text();
    }

    private String getAnnotation(Document bookPage) {
        Element annotation = bookPage.select("#product-about").first();
//        System.out.println(annotation.select("p").text());
        return annotation.select("p").text();
    }
}
