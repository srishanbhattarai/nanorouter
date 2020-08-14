package com.github.sb.nanorouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Backing data structure for the router to store a list of endpoints (for a single method).
 *
 * This is a fast data structure which allows O(k) insertion and search, where 'k' is the length
 * of the key under question.
 *
 * Invariants:
 *  1. Do not add two URLs that only differ in the name of the parameter. This does not make sense
 *  semantically, and is therefore an invariant in the data structure. (e.g "/books/:bookId"
 *  and "/books/:id"). Searching for "/books/123" in this case may non-deterministically yield a
 *  parameter name of either "bookId" or "id".
 *
 *  2. Trailing slashes are *strict* matches. e.g. /books/ and /books are two different URIs.
 */
class RouterTrie {
  // Parameters start with a ':' (e.g  ':id')
  private final char PARAM_START = ':';

  // The key stored at this node. If it is not a parameter, then it's length will be one.
  // Otherwise, it is the length of the parameter key.
  private String key;

  // True if this is a leaf node.
  private boolean is_leaf;

  // Set of children under this node.
  // Unfortunately, Java HashSets do not support 'get' operations
  // which is problematic for cases when for a HashSet<T>, hashCode is implemented over a type that is not T.
  // (such as this case). Therefore, we hackily resort to a HashMap<T, T>
  private HashMap<RouterTrie, RouterTrie> children;

  /**
   * Hash only based on the key. Child keys in a trie under any given parent are
   * guaranteed to be unique therefore this is safe.
   */
  @Override
  public int hashCode() {
    return this.key.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RouterTrie)) return false;

    RouterTrie other = (RouterTrie) o;

    return this.key.equals(other.key);
  }

  /**
   * A search operation on this structure will return one of these.
   */
  class Path {
    public HashMap<String, String> params;
    public String uri;

    Path(HashMap<String, String> params, String uri) {
      this.params = params;
      this.uri = uri;
    }
  }

  /**
   * Creates the root of the trie, which is a null node.
   */
  public RouterTrie() {
    this.key = null;
    this.is_leaf = false;
    this.children = new HashMap<>();
  }

  /**
   * Creates a node with the provided key.
   */
  public RouterTrie(String key) {
    this.key = key;
    this.is_leaf = false;
    this.children = new HashMap<>();
  }

  /**
   * Splits the string 's' into a List of strings according to the following criteria:
   *
   * For any characters that are not meant to be used as parameters, they will be left in tact
   * for all non-parameterized strings this method is effectively the same as 'toCharArray()'
   *
   * Otherwise, any characters that are part of parameters will be left intact.
   *
   * e.g. "/xy/:xyId/ab/:abId" is split into
   *        ["/", "x", "y", "/", ":xyId", "/", "a", "b", "/", ":abId"]
   */
  private List<String> split(String s) {
    ArrayList<String> result = new ArrayList<>();

    for (int i = 0; i < s.length();) {
      char key = s.charAt(i);

      // Parameter string found, jump to the next "/" or the end of string.
      if (key == PARAM_START) {
        int jmp = s.indexOf("/", i);
        jmp = jmp > 0 ? jmp : s.length();

        result.add(s.substring(i, jmp));
        i = jmp;
      } else {
        result.add(Character.toString(key));
        i++;
      }
    }

    return result;
  }

  /**
   * Returns true if this is a parametric node.
   */
  private boolean isParametric() {
    return this.key.startsWith(Character.toString(PARAM_START));
  }

  /**
   * Returns the parameter name at this node. Call this *only* for nodes for which isParametric()
   * returns true.
   *
   * Removes the leading PARAM_START and returns the rest of the key.
   */
  private String getParameter() {
    return this.key.substring(1);
  }

  /**
   * Try to find a node that contains 'key' as a descendant of 'node'. Otherwise, return null.
   * If there isn't a match for an exact field, but a parameterized entry exists then it will
   * be considered a match.
   *
   * e.g. if there are two entries in the trie:
   *   1. "/books/book/:id" and
   *   2. "/books/:id"
   * Then,
   *   1. "/books/book" will match the second entry (i.e id = "book")
   *   2. "/books/book/12" will match the first entry (i.e. id = "12").
   *
   * As mentioned in the invariants section in the class documentation, if there are two
   * parameterized fields at the same height (i.e. "/books/:id" and "/books/:bookId") this
   * method may return one or the other.
   */
  private RouterTrie find(String key, RouterTrie node) {
    RouterTrie result = node.children.get(new RouterTrie(key));
    if (result != null) return result;

    for (HashMap.Entry<RouterTrie, RouterTrie> c : node.children.entrySet()) {
      // Return a parametric child if we have one
      RouterTrie child = c.getValue();
      if (child.isParametric()) return child;
    }
    return null;
  }

  /**
   * Searches for the URI in the trie. If it exists, returns an object containing
   * the parameters. Otherwise, returns null.
   */
  public Path search(String uri) {
    HashMap<String, String> params = new HashMap<>();
    char[] parts = uri.toCharArray();

    // This will hold the URI that the trie stores i.e. this is the full URI string that
    // was inserted into the trie in the first place.
    String matchedPath = "";

    RouterTrie node = this;
    for (int i = 0; i < uri.length();) {
      String target = Character.toString(parts[i]);
      assert (target != null);

      // Try finding the target.
      RouterTrie next = find(target, node);
      if (next == null) return null; // Not found, the search target cannot exist

      // Save the parameter if it is a parametric node
      if (next.isParametric()) {
        int jmp = uri.indexOf("/", i);
        jmp = jmp > 0 ? jmp : uri.length();

        String param = next.getParameter();
        String paramValue = uri.substring(i, jmp);
        params.put(param, paramValue);

        i = jmp; // Jump to the next slash in the string
      } else {
        i++;
      }

      // Keep assembling this path as we traverse the trie.
      matchedPath = matchedPath.concat(next.key);

      node = next;
    }

    // If we got to this point, then we found the entire uri but it is not a leaf node, then it
    // is not a valid entry.
    //
    // If it is a leaf then construct the final Path to be returned.
    if (!node.is_leaf) return null;

    return new Path(params, matchedPath);
  }

  /**
   * Inserts the URI into the trie.
   *
   * @param uri
   */
  public void insert(String uri) {
    List<String> parts = split(uri);

    RouterTrie node = this;
    for (int i = 0; i < parts.size(); ++i) {
      String target = parts.get(i);
      assert (target != null);

      RouterTrie next = node.children.get(new RouterTrie(target));

      // Not found, create a child branch
      if (next == null) {
        next = new RouterTrie(target);
        node.children.put(next, next);
      }

      // Move onto next target
      node = next;
    }

    // At this point, 'node' contains the final key inserted. Mark it as a leaf node.
    node.is_leaf = true;
  }
}
