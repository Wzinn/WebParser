package main;

import dbservices.DBException;
import dbservices.DBService;
import parser.HTMLParser;

public class Main {

    public static void main(String[] args) {

        DBService dbService = new DBService();

        try {
            dbService.cleanUp();
        } catch (DBException e) {
            e.printStackTrace();
        }

        HTMLParser parser = new HTMLParser(dbService);
        parser.parse();
    }

}
