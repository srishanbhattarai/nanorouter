package com.github.sb.nanorouter;

import static org.junit.Assert.*;

import java.util.HashMap;
import org.junit.Test;

public class RouterTrieTest {

  @Test
  public void test_CheckInsertSearch_Matches() {
    // Found cases
    HashMap<String, String> empty = new HashMap<>();

    // CASE 1
    RouterTrie rt = new RouterTrie();
    rt.insert("/xy/:xyId/ab/:abId");

    RouterTrie.Path node = rt.search("/xy/123/ab/567");
    HashMap<String, String> expectedMap = new HashMap<>();
    expectedMap.put("xyId", "123");
    expectedMap.put("abId", "567");

    assert (node != null);
    assertEquals("/xy/:xyId/ab/:abId", node.uri);
    assert (node.params.equals(expectedMap));

    // CASE 2
    rt.insert("/xyz/abc");
    RouterTrie.Path node2 = rt.search("/xyz/abc");
    assert (node2 != null);
    assertEquals("/xyz/abc", node2.uri);
    assert (node2.params.equals(empty));
  }

  @Test
  public void test_CheckInsertSearch_NoMatches() {
    // Not found cases
    RouterTrie rt = new RouterTrie();

    rt.insert("/xyz/ab/:abId");
    RouterTrie.Path node = rt.search("/xyz/123/someId");
    assert (node == null);

    rt.insert("/xyz/:ab");
    RouterTrie.Path node2 = rt.search("/xyz/123/"); // do not match trailing slash
    assert (node2 == null);

    rt.insert("/123");
    RouterTrie.Path node3 = rt.search("/123/456");
    assert (node3 == null);
  }
}
