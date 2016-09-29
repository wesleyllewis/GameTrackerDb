package com.theironyard;

public class Game {
    int id;
    String name;
    String genre;
    String platform;
    int releaseYear;

    public Game(){}

    public Game(int id, String name, String genre, String platform, int releaseYear) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.platform = platform;
        this.releaseYear = releaseYear;
    }
}
