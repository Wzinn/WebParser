package main;

import dbservices.DBException;
import dbservices.DBService;
import dbservices.dataset.BooksDataSet;

public class Main {

    public static void main(String[] args) {

//        HTMLParser parser = new HTMLParser();

//        parser.parse();

        DBService dbService = new DBService();
        try {
            // String title, String authors, String translators, String publisher, String ISBN, String annotation
            long bookId = dbService.addBook("Zalupa", "Pupka", "Google translate", "PUB", "2831238", "Pupka za lupku");

            BooksDataSet dataSet = dbService.getBook(bookId);
            System.out.println("Book data set: " + dataSet);

            dbService.cleanUp();
        } catch (DBException e) {
            e.printStackTrace();
        }

    }

}
