# Nanorouter

[![Build Status](https://travis-ci.org/srishanbhattarai/nanorouter.svg?branch=master)](https://travis-ci.org/srishanbhattarai/nanorouter)

A small, transparent and performant routing library meant to be used with [NanoHTTPD](https://github.com/NanoHttpd/nanohttpd). 

This library adds support for route and method specific handlers, as well as basic templated URL parameter extraction.

# Usage
A complete example is in the `examples/` directory.

It takes very little plumbing to start using nanorouter:
1. Create a `Router` instance
```java
Router router = new Router();
```

2. Attach routes, and their respective `Handler` classes to the router
```java
router.addHandler(Method.GET, "/books/:bookID", new Handler() {
    @Override
    public Response handle(RequestContext ctx) {
        // Return the book Id from the URL as a response
        String bookId = ctx.params.getOrDefault("bookID", "N/A");
        return NanoHTTPD.newFixedLengthResponse(bookId);
    }
  }
});
```
3. Forward requests from NanoHTTPD's serve into the `Router`. You can choose to manage the instance of `NanoHTTPD` however you want to. In this example, I use inheritance.
```java
class MyServer extends NanoHTTPD {
  private final Router router;
  
  MyServer(Router router) {
    super("127.0.0.1", 9120);
    
    this.router = router;
  }
    
  @Override
  public Response serve(IHTTPSession session) {
    return this.router.handleRequest(this, session);
  }
}
```

5. Profit?

## Installation
This project uses GitHub releases, therefore installation via [Jitpack](https://jitpack.io/) is the easiest way to get started. For a Maven project:
```xml
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
	
	....
	
  <dependency>
    <groupId>com.github.srishanbhattarai</groupId>
    <artifactId>nanorouter</artifactId>
    <version>0.1.0</version>
  </dependency>
```

## Design and Performance
Nanorouter is intentionally minimal and fast for general purpose cases. Internally, it uses two primary data structures:

1. A HashMap which contains the registered handlers, organized by URI and HTTP method
2. A second HashMap which has a Trie for every HTTP method. This is used for `O(k)` matching of the URI where `k` is the length of the URI. In other words, the performance is *not* based on the number of URIs you choose to manage with this library, but only on the length of the URI that comes in at runtime.

Allocations in hot paths have been minimized, but there is still room for improvement. If you notice performance problems, please file an issue.

# License
NanoHTTPD was licensed under a "modified BSD License". The same license, as of [`41c44fe`](https://github.com/NanoHttpd/nanohttpd/tree/41c44fe4abf9722cf63d3f79791b4c26f9fb58be) has been applied to this project as well.
