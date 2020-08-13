package com.github.sb.nanorouter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {

  public static class Response {
    int statusCode;
    String body;

    Response(int code, String body) {
      this.statusCode = code;
      this.body = body;
    }
  }

  public static Response get(String target) {
    try {
      URL url = new URL(target);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(3000);
      conn.setReadTimeout(3000);

      int responseCode = conn.getResponseCode();

      InputStreamReader isr = responseCode < 300
        ? new InputStreamReader(conn.getInputStream())
        : new InputStreamReader(conn.getErrorStream());
      BufferedReader in = new BufferedReader(isr);
      String chunk;
      StringBuffer body = new StringBuffer();
      while ((chunk = in.readLine()) != null) body.append(chunk);
      in.close();

      return new Response(responseCode, body.toString());
    } catch (Exception e) {
      System.err.println("Something went wrong during GET: " + e.getMessage());
      e.printStackTrace();
      assert (false); // die
    }

    return null;
  }
}
