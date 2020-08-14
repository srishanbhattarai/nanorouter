package com.github.sb.nanorouterexample;

import com.github.sb.nanorouter.Router;
import fi.iki.elonen.NanoHTTPD;

public class RestApi extends NanoHTTPD {
  private Router router;

  RestApi(String hostname, int port, Router router) {
    super(hostname, port);
    this.router = router;
  }

  @Override
  public Response serve(IHTTPSession session) {
    return this.router.handleRequest(this, session);
  }
}
