package main;

import dbservices.DBException;
import dbservices.DBService;
import dbservices.dataset.BooksDataSet;
import parser.HTMLParser;

import java.util.ArrayList;
import java.util.Comparator;

public class Main {

    public static void main(String[] args) {

        DBService dbService = new DBService();

        try {
            dbService.cleanUp();
            dbService.createTables();
        } catch (DBException e) {
            e.printStackTrace();
        }

        HTMLParser parser = new HTMLParser(dbService);
        parser.parse();


        try {
            ArrayList<BooksDataSet> books = dbService.getAll();

            books.sort((o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));

            for (BooksDataSet book : books) {
                System.out.println(book);
            }

            System.out.println(dbService.getByTitle("введение"));
            System.out.println("====");
            System.out.println(dbService.getByAnnotation("Книга"));
            System.out.println("====");
            System.out.println(dbService.getByAuthor("Касиаро"));
        } catch (DBException e) {
            e.printStackTrace();
        }
    }

}
