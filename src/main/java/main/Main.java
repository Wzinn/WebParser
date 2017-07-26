package main;

import dbservices.BooksService;
import dbservices.DBException;
import dbservices.dataset.BooksDataSet;
import parser.HTMLParser;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        BooksService booksService = new BooksService();

        try {
            booksService.cleanUp();
        } catch (DBException e) {
            e.printStackTrace();
        }

        try {
            booksService.createTables();

            HTMLParser parser = new HTMLParser(booksService);
            parser.parse();
        } catch (DBException e) {
            e.printStackTrace();
        }

        try {
            ArrayList<BooksDataSet> books = booksService.getAll();

            books.sort((o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));

            for (BooksDataSet book : books) {
                System.out.println(book);
            }

            System.out.println("Search by title");
            System.out.println(booksService.getByTitle("введение"));
            System.out.println("Search by annotation");
            System.out.println(booksService.getByAnnotation("Узнайте об"));
            System.out.println("Search by author");
            System.out.println(booksService.getByAuthor("Касиаро"));
        } catch (DBException e) {
            e.printStackTrace();
        }
    }

}
