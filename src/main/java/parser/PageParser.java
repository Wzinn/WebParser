package parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class PageParser implements Runnable {

    private Elements books;

    public PageParser(Elements books) {
        this.books = books;
    }

    @Override
    public void run() {

        try {
            for (Element book : books) {

                Document bookPage = getBookPage(getLinkToBook(book));
                Element product = getProduct(bookPage);
                Elements redaction = getRedaction(product);

                getTitle(product);
                getAuthors(redaction);
                getTranslators(redaction);
                getPublisher(product);
                getIsbn(bookPage);
                getAnnotation(bookPage);

            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private static Document getBookPage(String link) throws IOException {
        return Jsoup.connect(link).get();
    }

    private static Element getProduct(Document bookPage) {
        return bookPage.select("div#product").first();
    }

    private static String getLinkToBook(Element element) {
        Element linkToBook = element.select(".cover").first();
        return "https://www.labirint.ru" + linkToBook.attr("href");
    }

    private static Elements getRedaction(Element product) {
        return product.select("div.authors");
    }

    private static String getTitle(Element product) {
        Element prodTitle = product.select(".prodtitle").first();
        String rusTitle = prodTitle.select("h1").first().text();
        System.out.println(rusTitle.substring(rusTitle.lastIndexOf(":") + 2));
//        String engTitle = (prodTitle.select("h2").first() != null) ? prodTitle.select("h2").first().text() : "Null";
        return rusTitle.substring(rusTitle.lastIndexOf(":") + 2);  // TODO pass to DB, comment out sout
    }

    private static ArrayList<String> getAuthors(Elements redaction) {
        ArrayList<String> list = new ArrayList<>();
        Elements authors = redaction.select("div:contains(Автор)"); // authors
        authors = authors.select("a");
        for (Element writer : authors) {
            list.add(writer.select("a").text());  // TODO pass to DB, comment out sout
            System.out.printf("Author: %s%n", writer.select("a").text());
        }
        return list;
    }

    private static String getTranslators(Elements redaction) {
        Elements translators = redaction.select("div:contains(Переводчик)");
        System.out.printf("Translator: %s%n", translators.select("a").text());
        return translators.select("a").text(); // TODO pass to DB, comment out sout
    }

    private static String getPublisher(Element product) {
        Elements publisher = product.select(".publisher");
        System.out.printf("Publisher: %s%n", publisher.select("a").text());
        return publisher.select("a").text(); // TODO pass to DB, comment out sout
    }

    private static String getIsbn(Document bookPage) {
        Element isbn = bookPage.select("div.isbn").first();
        System.out.println(isbn.text());
        return isbn.text(); // TODO pass to DB, comment out sout
    }

    private static String getAnnotation(Document bookPage) {
        Element annotation = bookPage.select("#product-about").first();
        System.out.println(annotation.select("p").text());
        return annotation.select("p").text(); // TODO pass to DB, comment out sout
    }

}
