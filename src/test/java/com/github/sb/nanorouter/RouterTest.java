package com.github.sb.nanorouter;

import static org.junit.Assert.*;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import java.io.IOException;
import org.junit.Test;

public class RouterTest {

  @Test
  public void test_Routing_Normal() {
    Router router = new Router();
    router.addHandler(
      Method.GET,
      "/books",
      new Handler() {

        @Override
        public Response handle(RequestContext ctx) {
          return NanoHTTPD.newFixedLengthResponse("books response");
        }
      }
    );

    // Create and start the server.
    TestServer s = new TestServer("127.0.0.1", 9120, router);
    try {
      s.start();
      HttpClient.Response resp = HttpClient.get("http://localhost:9120/books");
      assertEquals("books response", resp.body);
    } catch (IOException e) {
      System.err.print("Port already in use");
      assert (false);
    } finally {
      if (s.isAlive()) s.stop();
    }
  }

  @Test
  public void test_Routing_Parameterized() {
    Router router = new Router();
    router.addHandler(
      Method.GET,
      "/author/:authorId/books/:bookId",
      new Handler() {

        @Override
        public Response handle(RequestContext ctx) {
          String res =
            ctx.params.get("authorId") + "-" + ctx.params.get("bookId");
          return NanoHTTPD.newFixedLengthResponse(res);
        }
      }
    );

    TestServer s = new TestServer("127.0.0.1", 9120, router);
    try {
      s.start();
      HttpClient.Response resp = HttpClient.get(
        "http://localhost:9120/author/xyz/books/abc"
      );
      assertEquals("xyz-abc", resp.body);
    } catch (IOException e) {
      System.err.print("Port already in use");
      assert (false);
    } finally {
      if (s.isAlive()) s.stop();
    }
  }

  @Test
  public void test_Routing_404() {
    Router router = new Router();
    router.addHandler(
      Method.GET,
      "/authors",
      new Handler() {

        @Override
        public Response handle(RequestContext ctx) {
          return NanoHTTPD.newFixedLengthResponse("authors");
        }
      }
    );

    TestServer s = new TestServer("127.0.0.1", 9120, router);
    try {
      s.start();
      HttpClient.Response resp = HttpClient.get(
        "http://localhost:9120/author/xyz/books/abc"
      );
      assertEquals(404, resp.statusCode);
      assertEquals("Nanorouter: Route not found", resp.body);
    } catch (IOException e) {
      System.err.print("Port already in use");
      assert (false);
    } finally {
      if (s.isAlive()) s.stop();
    }
  }
}
