package com.theironyard;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();
    static int id;

    public static void main(String[] args) throws SQLException {
        Spark.init();
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS games (id IDENTITY, name VARCHAR, genre VARCHAR, platform VARCHAR, releaseYear INT )\n");


        Spark.get(
                "/",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    ArrayList<Game> gamesList = selectGames(conn);
                    HashMap m = new HashMap<>();

                    m.put("games", gamesList);
                    if (user == null) {
                        return new ModelAndView(m, "login.html");
                    } else {
                        return new ModelAndView(m, "home.html");
                    }

                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/create-user",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name);
                        users.put(name, user);
                    }

                    Session session = request.session();
                    session.attribute("userName", name);

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-game",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    if (user == null) {
                        //throw new Exception("User is not logged in");
                        Spark.halt(403);
                    }

                    String gameName = request.queryParams("gameName");
                    String gameGenre = request.queryParams("gameGenre");
                    String gamePlatform = request.queryParams("gamePlatform");
                    int gameYear = Integer.valueOf(request.queryParams("gameYear"));
//                    Game game = new Game(id, gameName, gameGenre, gamePlatform, gameYear);
                    HashMap m = new HashMap();

                    insertGame(conn, gameName, gameGenre, gamePlatform, gameYear);
                    m.put("id", id);


                    response.redirect("/");
                    return "";
                })
        );
        Spark.post("/edit-game", ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    if(user==null) {
                        throw new Exception("User is not logged in");
                    }
                    int id = Integer.parseInt(request.queryParams("id"));
                    String name = request.queryParams("gameName");
                    String genre = request.queryParams("gameGenre");
                    String gamePlatform = request.queryParams("gamePlatform");
                    int gameYear = Integer.parseInt(request.queryParams("gameYear"));
                    updateGame(conn, name, genre, gamePlatform, gameYear, id);

                    response.redirect("/");
                    return "";

                })
        );
        Spark.post("/delete-game", ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    if (user == null) {
                        throw new Exception("User is not logged in");
                    }
                    int id = Integer.valueOf(request.queryParams("id"));
                    deleteGame(conn, id);

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );
    }

Dst    static User getUserFromSession(Session session) {
        String name = session.attribute("userName");
        return users.get(name);
    }

    public static void insertGame(Connection conn, String name, String genre, String platform, int releaseYear) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO games VALUES (NULL, ?, ?, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, genre);
        stmt.setString(3, platform);
        stmt.setInt(4, releaseYear);
        stmt.execute();
    }

    public static void deleteGame(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM games WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();

    }

    public static ArrayList<Game> selectGames(Connection conn) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM games");
        while (results.next()) {
            int id = results.getInt("id");
            String name = results.getString("name");
            String genre = results.getString("genre");
            String platform = results.getString("platform");
            int gameYear = results.getInt("releaseYear");
            games.add(new Game(id, name, genre, platform, gameYear));
        }
        return games;
    }
    public static void updateGame(Connection conn, String name, String genre, String platform, int releaseYear, int id) throws SQLException{
        PreparedStatement stmt = conn.prepareStatement("UPDATE games TABLE SET  name=?, genre=?, platform=?, releaseYear=? WHERE id=?");
        stmt.setString(1, name);
        stmt.setString(2, genre);
        stmt.setString(3, platform);
        stmt.setInt(4, releaseYear);
        stmt.setInt(5, id);
        stmt.execute();
    }
}
