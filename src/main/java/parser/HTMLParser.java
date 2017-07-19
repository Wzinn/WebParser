package parser;

import java.io.*;
import java.util.ArrayList;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLParser {

    public static void main(String[] args) {

        final String URL = "https://www.labirint.ru/genres/2304";
        String newUrl = URL;
        Document doc;
        Document bookPage;

        try {
            while (true) {
                //get document
                doc = Jsoup.connect(newUrl).get();

                int lastPage = getLastPage(doc); // TODO

                // get links to books
                Elements innerContent = doc.select(".product-padding");

                for (Element element : innerContent) {
                    String link = getLinkToBook(element);
                    bookPage = getBookPage(link);
                    Element product = getProduct(bookPage);
                    String rusTitle = getTitle(product);
                    Elements redaction = getRedaction(product);

                    getAuthors(redaction);
                    getTranslators(redaction);
                    getPublisher(product);
                    getIsbn(bookPage);
                    getAnnotation(bookPage);
                }

                newUrl = getString(URL, doc);
                if (newUrl.equals(URL)) break; // exit on the last page
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Document getBookPage(String link) throws IOException {
        Document bookPage;// get a book's page
        bookPage = Jsoup.connect(link).get();
        return bookPage;
    }

    private static Element getProduct(Document bookPage) {
        // get product element
        return bookPage.select("div#product").first();
    }

    private static int getLastPage(Document doc) {
        int lastPage = Integer.parseInt(doc.select(".pagination-numbers a[href]").last().text());
        return lastPage;
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
//        String engTitle = (prodTitle.select("h2").first() != null) ? prodTitle.select("h2").first().text() : "Null";
        rusTitle = rusTitle.substring(rusTitle.lastIndexOf(":") + 2);  // TODO pass to DB, comment out sout
        return rusTitle;
    }

    private static String getString(String URL, Document doc) {
        String newUrl;// get the next page to parse
        Elements countPages = doc.select(".pagination-next");
        String nextUrl = countPages.select("a[href]").attr("href");
        newUrl = URL + nextUrl;
        return newUrl;
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
//        System.out.printf("Translator: %s%n", translators.select("a").text());
        return translators.select("a").text(); // TODO pass to DB, comment out sout
    }

    private static String getPublisher(Element product) {
        Elements publisher = product.select(".publisher");
//        System.out.printf("Publisher: %s%n", publisher.select("a").text());
        return publisher.select("a").text(); // TODO pass to DB, comment out sout
    }

    private static String getIsbn(Document bookPage) {
        Element isbn = bookPage.select("div.isbn").first();
        System.out.println(isbn.text());
        return isbn.text(); // TODO pass to DB, comment out sout
    }

    private static String getAnnotation(Document bookPage) {
        Element annotation = bookPage.select("#product-about").first();
//        System.out.println(annotation.select("p").text());
        return annotation.select("p").text(); // TODO pass to DB, comment out sout
    }

    private static void toHTML(String url, String name) {
        try {
            Document doc = Jsoup.connect(url).get();
            BufferedWriter out = new BufferedWriter(new FileWriter(name + ".html"));
            out.write(String.valueOf(doc));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
