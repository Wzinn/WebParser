package parser;

import dbservices.BooksService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class PageParser implements Runnable {

    private Elements pages;
    private BooksService booksService;

    PageParser(Elements pages, BooksService booksService) {
        this.pages = pages;
        this.booksService = booksService;
    }

    @Override
    public void run() {
        try {
            for (Element book : pages) {
                Document bookPage = getBookPage(getLinkToBook(book));
                new Thread(new BookParser(bookPage, booksService)).start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private Document getBookPage(String link) throws IOException {
        return Jsoup.connect(link).get();
    }

    private String getLinkToBook(Element element) {
        Element linkToBook = element.select(".cover").first();
        return "https://www.labirint.ru" + linkToBook.attr("href");
    }

}
