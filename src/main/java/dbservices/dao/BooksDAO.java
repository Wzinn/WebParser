package dbservices.dao;

import dbservices.dataset.BooksDataSet;
import dbservices.executor.Executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BooksDAO {

    private Executor executor;
    private final Connection connection;

    public BooksDAO(Connection connection) {
        this.executor = new Executor(connection);
        this.connection = connection;
    }

    public BooksDataSet get(long id) throws SQLException {
        return executor.execQuery("select * from books where id=" + id, result -> {
            result.next();
            return new BooksDataSet(
                    result.getLong(1),
                    result.getString(2),
                    result.getString(3),
                    result.getString(4),
                    result.getString(5),
                    result.getString(6),
                    result.getString(7));
        });
    }

    // TODO
    public long getBookId(String title) throws SQLException {
        return executor.execQuery("select * from books where title='" + title + "'", result -> {
            result.next();
            return result.getLong(1);
        });
    }

    public void insertBook(String title, String authors, String translators, String publisher, String ISBN, String annotation)
            throws SQLException {

        PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO books (title, authors, translators, publisher, ISBN, annotation) VALUES (?, ?, ?, ?, ?, ?)");
        pstmt.setString(1, title);
        pstmt.setString(2, authors);
        pstmt.setString(3, translators);
        pstmt.setString(4, publisher);
        pstmt.setString(5, ISBN);
        pstmt.setString(6, annotation);

        pstmt.execute();
        pstmt.close();

    }

    public void createTable() throws SQLException {
        executor.execUpdate("create table if not exists books " +
                "(id bigint auto_increment, " +
                "title varchar(256), " +
                "authors varchar(256), " +
                "translators varchar(256), " +
                "publisher varchar(256), " +
                "ISBN varchar(256), " +
                "annotation CLOB, " +
                "primary key (id))"
        );
    }

    public void dropTable() throws SQLException {
        executor.execUpdate("drop table books");
    }
}
