package com.vasiliska.DataMigration.models.mongo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "books")
@Getter
@Setter
public class Book {

    @Id
    private String id;

    private String bookName;

    private Author author;

    private Genre genre;

    public Book(String bookName, Author author, Genre genre) {
        this.bookName = bookName;
        this.author = author;
        this.genre = genre;
    }
}
