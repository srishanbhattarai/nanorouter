package com.github.sb.nanorouter;

import fi.iki.elonen.NanoHTTPD;
import java.util.HashMap;

/**
 * A RequestContext is provided to every handler which can be used to gain access to request
 * information (headers, body, params etc.), sessions, the NanoHTTPD class instance etc.
 */
public class RequestContext {
  /**
   * URI parameters if any.
   *
   * E.g. If the registered handler URI was "/author/:authorId/books/:booksId" then this will contain
   * the "authorId", and "booksId" values from the matched request URI.
   *
   * If this request did not match any registered URI, then this will be null.
   */
  public HashMap<String, String> params;

  /**
   * The original NanoHTTPD instance that the router is operating under.
   */
  public NanoHTTPD server;

  /**
   * The current request session.
   */
  public NanoHTTPD.IHTTPSession session;

  /**
   * The method for this request. This is the same as 'session.getMethod()'.
   */
  public NanoHTTPD.Method method;

  /**
   * The URI for this current request. This is the same as 'session.getUri()'.
   */
  public String uri;

  /**
   * The original URI that was registered to a handler if there was a match. Otherwise, null.
   */
  public String registeredUri;
}
