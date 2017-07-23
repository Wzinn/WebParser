package dbservices.dao;

import dbservices.dataset.BooksDataSet;
import dbservices.executor.Executor;

import java.sql.*;
import java.util.ArrayList;

public class BooksDAO {

    private Executor executor;
    private final Connection connection;
    private ArrayList<Long> authorsId;

    public BooksDAO(Connection connection) {
        this.executor = new Executor(connection);
        this.connection = connection;
    }

    public BooksDataSet get(long id) throws SQLException {
        return executor.execQuery("select web_parser.books.id, web_parser.books.title, web_parser.authors.name as author, \n" +
                "web_parser.translators.name as translator, web_parser.publishers.name as publisher,  \n" +
                "web_parser.books.isbn, web_parser.books.annotation \n" +
                "from web_parser.books_authors\n" +
                "\tjoin web_parser.authors on web_parser.books_authors.author_id = web_parser.authors.id\n" +
                "\tjoin web_parser.books on web_parser.books_authors.book_id = web_parser.books.id\n" +
                "\tjoin web_parser.translators on web_parser.books.translator_id = web_parser.translators.id\n" +
                "\tjoin web_parser.publishers on web_parser.books.publisher_id = web_parser.publishers.id\n" +
                "    where web_parser.books.id =" + id, result -> {

            ArrayList<String> authors = new ArrayList<>();

            result.next();

            String title = result.getString(2);
            authors.add(result.getString(3));
            String translators = result.getString(4);
            String publisher = result.getString(5);
            String ISBN = result.getString(6);
            String annotation = result.getString(7);

            while (result.next()) {
                authors.add(result.getString(3));
            }
            return new BooksDataSet(id, title, authors, translators, publisher, ISBN, annotation);
        });
    }

    public ArrayList<BooksDataSet> getAll() throws SQLException {
        int rowCount = executor.execQuery("SELECT * FROM web_parser.books", result -> {
            result.last();
            return result.getRow();
        });

        ArrayList<BooksDataSet> list = new ArrayList<>(rowCount);

        for (int i = 1; i <= rowCount; i++ ) {
            list.add(get(i));
        }
        return list;
    }

    public ArrayList<BooksDataSet> getByTitle(String title) throws SQLException {
        return executor.execQuery(
                "SELECT web_parser.books.id  FROM web_parser.books\n" +
                        "where web_parser.books.title COLLATE UTF8_GENERAL_CI LIKE \"%" + title + "%\"", result -> {

            ArrayList<BooksDataSet> list = new ArrayList<>();
            while (result.next()) {
                list.add(get(result.getInt(1)));
            }
            return list;
        });
    }

    public ArrayList<BooksDataSet> getByAuthor(String author) throws SQLException {
        return executor.execQuery(
                "select web_parser.books.id from web_parser.books_authors\n" +
                        "\tjoin web_parser.authors on web_parser.books_authors.author_id = web_parser.authors.id\n" +
                        "\tjoin web_parser.books on web_parser.books_authors.book_id = web_parser.books.id\n" +
                        "    where web_parser.authors.name COLLATE UTF8_GENERAL_CI LIKE '%" + author + "%'", result -> {

                    ArrayList<BooksDataSet> list = new ArrayList<>();
                    while (result.next()) {
                        list.add(get(result.getInt(1)));
                    }
                    return list;
                });
    }

    public ArrayList<BooksDataSet> getByAnnotation(String annotation) throws SQLException {
        return executor.execQuery(
                "SELECT web_parser.books.id  FROM web_parser.books\n" +
                        "where web_parser.books.annotation COLLATE UTF8_GENERAL_CI LIKE \"%" + annotation + "%\"", result -> {
                ArrayList<BooksDataSet> list = new ArrayList<>();
                while (result.next()) {
                    list.add(get(result.getInt(1)));
                }
                return list;
                });
    }

    public synchronized void insertBook(String title, ArrayList<String> authors, String translators, String publisher, String ISBN, String annotation)
            throws SQLException {

        getOrCreateAuthor(authors);
        getOrCreateTranslator(translators);
        getOrCreatePublisher(publisher);

        PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO books (title, translator_id, publisher_id, ISBN, annotation) " +
                        "VALUES (? , @last_inserted_tran_id, @last_inserted_pub_id, ?, ?)"
        );
        pstmt.setString(1, title);
        pstmt.setString(2, ISBN);
        pstmt.setString(3, annotation);
        pstmt.execute();
        pstmt.close();

        executor.execUpdate("SET @last_inserted_id = LAST_INSERT_ID()");

        if (!authorsId.isEmpty()) {
            for (Long authorId : authorsId) {
                executor.execUpdate("INSERT INTO books_authors (book_id, author_id) VALUES (@last_inserted_id, " + authorId + ")");
            }
        } else {
            executor.execUpdate("INSERT INTO books_authors (book_id, author_id) VALUES (@last_inserted_id, " + 1 + ")");
        }
    }

    private void getOrCreateAuthor(ArrayList<String> authors) throws SQLException {
        authorsId = new ArrayList<>(authors.size());
        long id = 0;
            for (String author : authors) {
                long authorId = getAuthorId(author);
                // check if author is already exist in DB
                if(authorId == 0L) {
                    PreparedStatement pstmt = connection.prepareStatement("INSERT INTO authors (name) VALUE (?)", Statement.RETURN_GENERATED_KEYS);
                    pstmt.setString(1, author);
                    pstmt.execute();

                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        id = rs.getLong(1);
                    }
                    pstmt.close();
                    authorsId.add(id);
                } else {
                    authorsId.add(authorId);
                }
            }
    }

    private void getOrCreateTranslator(String translators) throws SQLException {
        long translatorId = getTranslatorsId(translators);
        if (translatorId == 0L) {
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO translators (name) VALUE (?)");
            pstmt.setString(1, translators);
            pstmt.execute();
            pstmt.close();
            executor.execUpdate("SET @last_inserted_tran_id = LAST_INSERT_ID()");
        } else {
            executor.execUpdate("SET @last_inserted_tran_id = " + translatorId);
        }
    }

    private void getOrCreatePublisher(String publisher) throws SQLException {
        long publisherId = getPublisherId(publisher);
        if (publisherId == 0L) {
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO publishers (name) VALUE (?)");
            pstmt.setString(1, publisher);
            pstmt.execute();
            pstmt.close();
            executor.execUpdate("SET @last_inserted_pub_id = LAST_INSERT_ID()");
        } else {
            executor.execUpdate("SET @last_inserted_pub_id = " + publisherId);
        }
    }

    private long getAuthorId(String authors) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery("SELECT id FROM web_parser.authors where name ='" + authors + "'");
        if (!result.next()) {
            return 0L;
        } else {
            return result.getLong(1);
        }
    }

    private long getTranslatorsId(String translators) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery("SELECT id FROM web_parser.translators where name ='" + translators + "'");
        if (!result.next()) {
            return 0L;
        } else {
            return result.getLong(1);
        }
    }

    private long getPublisherId(String publisher) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery("SELECT id FROM web_parser.publishers where name ='" + publisher + "'");
        if (!result.next()) {
            return 0L;
        } else {
            return result.getLong(1);
        }
    }

    public void createTable() throws SQLException {
        // create tables authors, translators, publishers

        executor.execUpdate(
                "CREATE TABLE IF NOT EXISTS `web_parser`.`authors` (\n" +
                        "  `id` INT NOT NULL AUTO_INCREMENT,\n" +
                        "  `name` VARCHAR(255) NULL,\n" +
                        "  PRIMARY KEY (`id`), \n" +
                        "  UNIQUE INDEX `name_UNIQUE` (`name` ASC))"
        );

        executor.execUpdate(
                "INSERT INTO `web_parser`.`authors` (`name`) VALUES (null);"
        );

        executor.execUpdate(
                "CREATE TABLE IF NOT EXISTS `web_parser`.`translators` (\n" +
                        "    `id` INT NOT NULL AUTO_INCREMENT,\n" +
                        "    `name` VARCHAR(255) NULL,\n" +
                        "    PRIMARY KEY (`id`))"
        );

        executor.execUpdate(
                "CREATE TABLE IF NOT EXISTS `web_parser`.`publishers` (\n" +
                        "      `id` INT NOT NULL AUTO_INCREMENT,\n" +
                        "      `name` VARCHAR(255) NULL,\n" +
                        "      PRIMARY KEY (`id`))"
        );

        // create table books
        executor.execUpdate(
                "CREATE TABLE IF NOT EXISTS `web_parser`.`books` (\n" +
                        "    `id` INT NOT NULL AUTO_INCREMENT,\n" +
                        "    `title` VARCHAR(255) NOT NULL,\n" +
                        "    `translator_id` INT NULL,\n" +
                        "    `publisher_id` INT NOT NULL,\n" +
                        "    `ISBN` VARCHAR(255) NOT NULL,\n" +
                        "    `annotation` VARCHAR(16384) NOT NULL,\n" +
                        "    PRIMARY KEY (`id`),\n" +
                        "    UNIQUE INDEX `ISBN_name_UNIQUE` (`ISBN` ASC),\n" +
                        "    INDEX `fk_books_translators_idx` (`translator_id` ASC),\n" +
                        "    INDEX `fk_books_publishers_idx` (`publisher_id` ASC),\n" +
                        "    CONSTRAINT `fk_books_translators`\n" +
                        "      FOREIGN KEY (`translator_id`)\n" +
                        "      REFERENCES `web_parser`.`translators` (`id`)\n" +
                        "      ON DELETE CASCADE\n" +
                        "      ON UPDATE CASCADE,\n" +
                        "    CONSTRAINT `fk_books_publishers`\n" +
                        "      FOREIGN KEY (`publisher_id`)\n" +
                        "      REFERENCES `web_parser`.`publishers` (`id`)\n" +
                        "      ON DELETE CASCADE\n" +
                        "      ON UPDATE CASCADE)"
        );

        // create table books_authors
        executor.execUpdate(
                "CREATE TABLE IF NOT EXISTS `web_parser`.`books_authors` (\n" +
                        "  `book_id` INT NOT NULL,\n" +
                        "  `author_id` INT NOT NULL,\n" +
                        "  PRIMARY KEY (`book_id`, `author_id`),\n" +
                        "  INDEX `fk_books_authors_author_idx` (`author_id` ASC),\n" +
                        "  CONSTRAINT `fk_books_authors_book`\n" +
                        "    FOREIGN KEY (`book_id`)\n" +
                        "    REFERENCES `web_parser`.`books` (`id`)\n" +
                        "    ON DELETE CASCADE\n" +
                        "    ON UPDATE CASCADE,\n" +
                        "  CONSTRAINT `fk_books_authors_author`\n" +
                        "    FOREIGN KEY (`author_id`)\n" +
                        "    REFERENCES `web_parser`.`authors` (`id`)\n" +
                        "    ON DELETE CASCADE\n" +
                        "    ON UPDATE CASCADE)"
        );
    }

    public void dropTable() throws SQLException {
        executor.execUpdate("drop table books_authors");
        executor.execUpdate("drop table books");
        executor.execUpdate("drop table authors");
        executor.execUpdate("drop table translators");
        executor.execUpdate("drop table publishers");
    }
}
