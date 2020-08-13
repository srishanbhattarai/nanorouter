package com.github.sb.nanorouter;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

/**
 * This is the default handler called when a request for an unrecognized Route/Method pair arrives.
 */
public class DefaultHandler extends Handler {

  public NanoHTTPD.Response handle(RequestContext ctx) {
    return NanoHTTPD.newFixedLengthResponse(
      Status.NOT_FOUND,
      "application/text",
      "Nanorouter: Route not found"
    );
  }
}
