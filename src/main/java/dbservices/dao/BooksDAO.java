package dbservices.dao;

import dbservices.dataset.BooksDataSet;
import dbservices.executor.Executor;
import dbservices.executor.ResultHandler;

import java.sql.*;
import java.util.ArrayList;

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



    public synchronized void insertBook(String title, String authors, String translators, String publisher, String ISBN, String annotation)
            throws SQLException {

        /*

        // TODO:
        INSERT INTO authors (name) VALUE ('Pushkin');
        SET @last_inserted_author_id = LAST_INSERT_ID();

        //TODO:
        INSERT INTO translators (name) value ('Google translate');
        SET @last_inserted_tran_id = LAST_INSERT_ID();

        //TODO:
        INSERT INTO publishers (name) value ('Peter');
        SET @last_inserted_pub_id = LAST_INSERT_ID();

         //TODO: done
        INSERT INTO books (title, translator_id, publisher_id, ISBN, annotation)
        VALUES ('SQL for beginners', @last_inserted_tran_id, @last_inserted_pub_id, 'ISBN 21312', 'Book for dummies');
        SET @last_inserted_id = LAST_INSERT_ID();

         //TODO: done
        INSERT INTO books_authors (book_id, author_id) VALUES (@last_inserted_id, @last_inserted_author_id);
         */


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
        executor.execUpdate("INSERT INTO books_authors (book_id, author_id) VALUES (@last_inserted_id, @last_inserted_author_id)");
    }

    private void getOrCreateAuthor(String authors) throws SQLException {
        long authorId = getAuthorId(authors);
        if(authorId == 0L) {
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO authors (name) VALUE (?)");
            pstmt.setString(1, authors);
            pstmt.execute();
            pstmt.close();
            executor.execUpdate("SET @last_inserted_author_id = LAST_INSERT_ID()");
        } else {
            executor.execUpdate("SET @last_inserted_author_id = " + authorId);
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

    public void createCTable() throws SQLException {
        // create tables authors, translators, publishers

        executor.execUpdate(
                "CREATE TABLE IF NOT EXISTS `web_parser`.`authors` (\n" +
                        "  `id` INT NOT NULL AUTO_INCREMENT,\n" +
                        "  `name` VARCHAR(255) NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE INDEX `name_UNIQUE` (`name` ASC))"
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
                        "    `annotation` VARCHAR(8192) NOT NULL,\n" +
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
