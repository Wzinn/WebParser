package dbservices;

import dbservices.dao.BooksDAO;
import dbservices.dataset.BooksDataSet;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBService {

    private final Connection connection;

    public DBService() {
        this.connection = getMysqlConnection();
    }

    public BooksDataSet getBook(long id) throws DBException {
        try {
            return (new BooksDAO(connection).get(id));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public ArrayList<BooksDataSet> getByTitle(String title) throws DBException {
        try {
            return (new BooksDAO(connection).getByTitle(title));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public ArrayList<BooksDataSet> getByAuthor(String author) throws DBException {
        try {
            return (new BooksDAO(connection).getByAuthor(author));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }


    public ArrayList<BooksDataSet> getByAnnotation(String annotation) throws DBException {
        try {
            return (new BooksDAO(connection).getByAnnotation(annotation));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public ArrayList<BooksDataSet> getAll() throws DBException {
        try {
            return (new BooksDAO(connection).getAll());
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public synchronized void addBook(String title, ArrayList authors, String translators, String publisher, String ISBN, String annotation)
            throws DBException {
        try {
            connection.setAutoCommit(false); // disable autocommit to work with transactions
            BooksDAO dao = new BooksDAO(connection);
            dao.insertBook(title, authors, translators, publisher, ISBN, annotation);
            connection.commit();
//            return dao.getBookId(title);
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

    private static Connection getMysqlConnection() {
        try {
            DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());
            StringBuilder url = new StringBuilder();
            url.
                    append("jdbc:mysql://").        //db type
                    append("localhost:").           //host name
                    append("3306/").                //port
                    append("web_parser?").          //db name
                    append("user=root&").          //login
                    append("password=password&"). // password
                    append("characterEncoding=utf8&"). // encoding
                    append("relaxAutoCommit=true"); // relaxAutoCommit

            System.out.println("URL: " + url + "\n");

            return DriverManager.getConnection(url.toString());
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
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

    public void createTables() throws DBException {
        BooksDAO dao = new BooksDAO(connection);
        try {
            dao.createTable();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

}
