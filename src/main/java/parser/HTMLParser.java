package parser;

import java.io.*;

import dbservices.DBService;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HTMLParser {

    private DBService dbService;

    private final String URL = "https://www.labirint.ru/genres/2304?page=";

    public HTMLParser(DBService dbService) {
        this.dbService = dbService;
    }

    public void parse() {
        try {
            int page = 1;

            while (true) {
                Document doc = Jsoup.connect(URL + page).get();
                Elements books = doc.select(".product-padding");
                int lastPage = getLastPage(doc);

                new Thread(new PageParser(books, dbService)).start();

                page++;
                if (page == lastPage) break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getLastPage(Document doc) {
        return Integer.parseInt(doc.select(".pagination-numbers a[href]").last().text());
    }

}
