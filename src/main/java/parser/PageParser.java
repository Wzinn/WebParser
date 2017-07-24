package parser;

import dbservices.DBService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class PageParser implements Runnable {

    private Elements pages;
    private DBService dbService;

    PageParser(Elements pages, DBService dbService) {
        this.pages = pages;
        this.dbService =  dbService;
    }

    @Override
    public void run() {
        try {
            for (Element book : pages) {
                Document bookPage = getBookPage(getLinkToBook(book));
                new Thread(new BookParser(bookPage, dbService)).start();
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
