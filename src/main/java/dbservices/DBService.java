package dbservices;

import dbservices.dao.BooksDAO;
import dbservices.dataset.BooksDataSet;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBService {

    private final Connection connection;

    public DBService() {
        this.connection = getH2Connection();
    }

    public BooksDataSet getBook(long id) throws DBException {
        try {
            return (new BooksDAO(connection).get(id));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public long addBook(String title, String authors, String translators, String publisher, String ISBN, String annotation)
            throws DBException {
        try {
            connection.setAutoCommit(false); // disable autocommit to work with transactions
            BooksDAO dao = new BooksDAO(connection);
            dao.createTable();
            dao.insertBook(title, authors, translators, publisher, ISBN, annotation);
            connection.commit();
            return dao.getBookId(title);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            throw new DBException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }
    }

    private static Connection getH2Connection() {
        try {
            String url = "jdbc:h2:./h2db";
            String name = "sa";
            String pass = "password";

            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL(url);
            ds.setUser(name);
            ds.setPassword(pass);

            return DriverManager.getConnection(url, name, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cleanUp() throws DBException {
        BooksDAO dao = new BooksDAO(connection);
        try {
            dao.dropTable();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

}
