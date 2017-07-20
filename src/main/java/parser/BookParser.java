package parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class BookParser implements Runnable {

    private Document book;

    BookParser(Document book) {
        this.book = book;
    }

    @Override
    public void run() {
        Element product = getProduct(book);
        Elements redaction = getRedaction(product);

        getTitle(product);
        getAuthors(redaction);
        getTranslators(redaction);
        getPublisher(product);
        getIsbn(book);
        getAnnotation(book);
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
        return rusTitle.substring(rusTitle.lastIndexOf(":") + 2);  // TODO pass to DB, comment out sout
    }

    private ArrayList<String> getAuthors(Elements redaction) {
        ArrayList<String> list = new ArrayList<>();
        Elements authors = redaction.select("div:contains(Автор)"); // authors
        authors = authors.select("a");
        for (Element writer : authors) {
            list.add(writer.select("a").text());  // TODO pass to DB, comment out sout
            System.out.printf("Author: %s%n", writer.select("a").text());
        }
        return list;
    }

    private String getTranslators(Elements redaction) {
        Elements translators = redaction.select("div:contains(Переводчик)");
        System.out.printf("Translator: %s%n", translators.select("a").text());
        return translators.select("a").text(); // TODO pass to DB, comment out sout
    }

    private String getPublisher(Element product) {
        Elements publisher = product.select(".publisher");
        System.out.printf("Publisher: %s%n", publisher.select("a").text());
        return publisher.select("a").text(); // TODO pass to DB, comment out sout
    }

    private String getIsbn(Document bookPage) {
        Element isbn = bookPage.select("div.isbn").first();
        System.out.println(isbn.text());
        return isbn.text(); // TODO pass to DB, comment out sout
    }

    private String getAnnotation(Document bookPage) {
        Element annotation = bookPage.select("#product-about").first();
        System.out.println(annotation.select("p").text());
        return annotation.select("p").text(); // TODO pass to DB, comment out sout
    }
}
