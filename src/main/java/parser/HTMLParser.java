package parser;

import java.io.*;

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
//                System.out.println(newUrl);

                //get document
                doc = Jsoup.connect(newUrl).get();

                // get links to books
                Elements innerContent = doc.select(".product-padding");

                for (Element element : innerContent) {
                    Element linkToBook = element.select(".cover").first();
                    String link = "https://www.labirint.ru" + linkToBook.attr("href"); // link to a book page

                    // get a book's page
                    bookPage = Jsoup.connect(link).get();

                    // get product element
                    Element product = bookPage.select("div#product").first();

                    // get book's tittle (rus and eng)
                    Element prodTitle = product.select(".prodtitle").first();
                    String rusTitle = prodTitle.select("h1").first().text();
                    String engTitle = (prodTitle.select("h2").first() != null) ? prodTitle.select("h2").first().text() : "Null";
                    rusTitle = rusTitle.substring(rusTitle.lastIndexOf(":") + 2);  // TODO pass to DB, comment out sout

                    System.out.printf("Title: %s%n", rusTitle);
                    System.out.printf("Eng title: %s%n", engTitle);

                    // get redaction info: author(s), translator, publisher
                    Elements redaction = product.select("div.authors"); // 1) authors; 2) translators; 3) editor

                    // get authors
                    Elements authors = redaction.select("div:contains(Автор)"); // authors
                    authors = authors.select("a");

                    // get each author's name
                    for (Element writer : authors) {
                        writer.select("a").text();  // TODO pass to DB, comment out sout
                        System.out.printf("Author: %s%n", writer.select("a").text());
                    }

                    // get translator name
                    Elements translators = redaction.select("div:contains(Переводчик)");
                    translators.select("a").text(); // TODO pass to DB, comment out sout
                    System.out.printf("Translator: %s%n", translators.select("a").text());

                    // get publisher
                    Elements publisher = product.select(".publisher");
                    publisher.select("a").text(); // TODO pass to DB, comment out sout
                    System.out.printf("Publisher: %s%n", publisher.select("a").text());

                    // get isbn
                    Element isbn = bookPage.select("div.isbn").first();
                    isbn.text(); // TODO pass to DB, comment out sout
                    System.out.println(isbn.text());

                    // get annotation
                    Element annotation = bookPage.select("#product-about").first();
                    annotation.select("p").text(); // TODO pass to DB, comment out sout
                    System.out.println(annotation.select("p").text());
                }

                // get the next page to parse
                Elements countPages = doc.select(".pagination-next");
                String nextUrl = countPages.select("a[href]").attr("href");
                newUrl = URL + nextUrl;

                if (newUrl.equals(URL)) break; // exit on the last page
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
