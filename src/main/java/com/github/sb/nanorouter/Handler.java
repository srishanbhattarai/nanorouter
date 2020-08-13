package com.github.sb.nanorouter;

import fi.iki.elonen.NanoHTTPD;

public abstract class Handler {

  public abstract NanoHTTPD.Response handle(RequestContext ctx);
}
