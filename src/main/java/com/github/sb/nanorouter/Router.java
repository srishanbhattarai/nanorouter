package com.github.sb.nanorouter;

import fi.iki.elonen.NanoHTTPD;
import java.util.HashMap;

public class Router {
  // Holds registered handlers for each method + URI pair
  private HashMap<NanoHTTPD.Method, HashMap<String, Handler>> handlers;

  // Holds registered URIs for each method for fast lookups
  private HashMap<NanoHTTPD.Method, RouterTrie> tries;

  // If a request does not match a registered handler, then invoke this one.
  private Handler defaultHandler;

  /**
   * The default constructor for the Router.
   *
   * Notes on space allocation:
   * By default, this makes two HashMap allocations for each permissible HTTP method.
   * Lazy initialization when attempting to locate the right handler for a request is avoided to skip a branch on every request.
   */
  public Router() {
    this.defaultHandler = new DefaultHandler();
    this.handlers = new HashMap<>();
    this.tries = new HashMap<>();

    for (NanoHTTPD.Method m : NanoHTTPD.Method.values()) {
      this.handlers.put(m, new HashMap<String, Handler>());
      this.tries.put(m, new RouterTrie());
    }
  }

  /**
   * Register the handler for the given Method and URI.
   */
  public void addHandler(NanoHTTPD.Method method, String uri, Handler h) {
    // Null safety: handlers and tries are guaranteed to contain an entry for each method
    this.handlers.get(method).put(uri, h);
    this.tries.get(method).insert(uri);
  }

  /**
   * Customize the default handler.
   */
  public void setDefaultHandler(Handler h) {
    this.defaultHandler = h;
  }

  /**
   * Forward your serve requests to this method.
   *
   * This method guarantees to call any valid registered handlers for this endpoint, and
   * the Response from that endpoint will be sent as a reply to the session.
   */
  public NanoHTTPD.Response handleRequest(
    NanoHTTPD server,
    NanoHTTPD.IHTTPSession session
  ) {
    NanoHTTPD.Method m = session.getMethod();
    String uri = session.getUri();

    RouterTrie.Path path = this.tries.get(m).search(uri);

    // Prepare context for the handler
    RequestContext ctx = new RequestContext();
    ctx.server = server;
    ctx.method = m;
    ctx.uri = uri;
    ctx.session = session;

    if (path == null) {
      ctx.params = null;
      ctx.registeredUri = null;

      return this.defaultHandler.handle(ctx);
    }

    // matching path was found
    ctx.params = path.params;
    ctx.registeredUri = path.uri;

    Handler h = this.handlers.get(m).get(path.uri);

    return h.handle(ctx);
  }
}
