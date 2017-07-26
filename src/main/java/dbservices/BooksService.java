package dbservices;

import dbservices.dao.BooksDAO;
import dbservices.dataset.BooksDataSet;

import java.sql.SQLException;
import java.util.ArrayList;

public class BooksService {

    private BooksDAO dao;

    public BooksService() {
        dao = BooksDAO.getInstance();
    }

    public BooksDataSet getBook(long id) throws DBException {
        try {
            return dao.get(id);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public ArrayList<BooksDataSet> getByTitle(String title) throws DBException {
        try {
            return dao.getByTitle(title);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public ArrayList<BooksDataSet> getByAuthor(String author) throws DBException {
        try {
            return dao.getByAuthor(author);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }


    public ArrayList<BooksDataSet> getByAnnotation(String annotation) throws DBException {
        try {
            return dao.getByAnnotation(annotation);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public ArrayList<BooksDataSet> getAll() throws DBException {
        try {
            return dao.getAll();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public synchronized void addBook(String title, ArrayList<String> authors, String translators, String publisher, String ISBN, String annotation)
            throws DBException {
        dao.insertBook(title, authors, translators, publisher, ISBN, annotation);
    }

    public void cleanUp() throws DBException {
        try {
            dao.dropTable();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public void createTables() throws DBException {
        try {
            dao.createTable();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

}
