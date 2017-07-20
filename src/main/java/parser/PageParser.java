package parser;

import dbservices.DBService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PageParser implements Runnable {

    private Elements pages;
    private Executor executor;
    private DBService dbService;

    public PageParser(Elements pages, DBService dbService) {
        this.pages = pages;
        executor = Executors.newFixedThreadPool(10);
        this.dbService =  dbService;
    }

    @Override
    public void run() {

        try {
            for (Element book : pages) {
                Document bookPage = getBookPage(getLinkToBook(book));
                executor.execute(new BookParser(bookPage, dbService));
//                new Thread(new BookParser(bookPage)).start();
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
