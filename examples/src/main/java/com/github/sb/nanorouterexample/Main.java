package com.github.sb.nanorouterexample;

import com.github.sb.nanorouter.Handler;
import com.github.sb.nanorouter.RequestContext;
import com.github.sb.nanorouter.Router;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.io.IOException;
import java.util.HashMap;

public class Main {
  static HashMap<String, String> bookPerUser = new HashMap<>();

  static void seedDatabase() {
    // Dummy database
    bookPerUser.put("Alice", "book1");
    bookPerUser.put("Bob", "book2");
  }

  static Router createRouter() {
    // Create router and attach handlers
    Router router = new Router();

    router.setDefaultHandler(
      new Handler() {

        public Response handle(RequestContext requestContext) {
          return NanoHTTPD.newFixedLengthResponse(
            Status.NOT_FOUND,
            "application/json",
            "{\"status\": \"Not found\"}"
          );
        }
      }
    );

    // This route returns all books from the database by (terribly) creating a space separated string.
    router.addHandler(
      Method.GET,
      "/books",
      new Handler() {

        public Response handle(RequestContext ctx) {
          String allBooks = "";
          for (String book : bookPerUser.values()) allBooks += book + " ";

          return NanoHTTPD.newFixedLengthResponse(
            Status.OK,
            "application/text",
            allBooks
          );
        }
      }
    );

    // This returns the book for that one person
    router.addHandler(
      Method.GET,
      "/books/:id",
      new Handler() {

        public Response handle(RequestContext ctx) {
          String id = ctx.params.getOrDefault("id", "N/A");
          String book = bookPerUser.getOrDefault(id, "N/A");

          return NanoHTTPD.newFixedLengthResponse(
            Status.OK,
            "application/text",
            book
          );
        }
      }
    );

    return router;
  }

  public static void main(String[] args) {
    seedDatabase();
    Router router = createRouter();
    RestApi server = new RestApi("127.0.0.1", 9120, router);

    try {
      server.start();
      System.out.println("Server is running at http://127.0.0.1:9120...");
      while (true) {}
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Could not start server: " + e.getMessage());
    }
  }
}
