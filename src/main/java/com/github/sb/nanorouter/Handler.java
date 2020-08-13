package com.github.sb.nanorouter;

import fi.iki.elonen.NanoHTTPD;

public interface Handler {
  public NanoHTTPD.Response handle(RequestContext ctx);
}
