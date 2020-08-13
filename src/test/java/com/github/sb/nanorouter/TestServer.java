package com.github.sb.nanorouter;

import fi.iki.elonen.NanoHTTPD;

public class TestServer extends NanoHTTPD {
  public final Router router;

  public TestServer(String hostname, int port, Router router) {
    super(hostname, port);
    this.router = router;
  }

  @Override
  public Response serve(IHTTPSession session) {
    return router.handleRequest(this, session);
  }
}
