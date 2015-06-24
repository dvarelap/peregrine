package com.github.dvarelap.stilt

import com.twitter.util.Await

class CookieBasedSessionSpec extends ShouldSpec {

  "#get" should "return Some(String) if there's a value with the corresponding key" in {
    val session = new CookieBasedSession("TEST_ID")
    Await.result(session.put("foo", "bar"))
    Await.result(session.get("foo")) should be (Some("bar"))
  }

  "#get" should "return None if there's a value with the corresponding key" in {
    val session = new CookieBasedSession("TEST_ID")
    Await.result(session.put("foo", "bar"))
    Await.result(session.get("zaa")) should be (None)
  }

  "#set" should "set a valid value in session object" in {
    val session = new CookieBasedSession("TEST_ID")
    Await.result(session.put("foo", "bar"))
    Await.result(session.get("foo")) should be (Some("bar"))
  }

  "#del" should "delete a value with the exsiting key" in {
    val session = new CookieBasedSession("TEST_ID")
    Await.result(session.put("foo", "bar"))
    Await.result(session.get("foo")) should be (Some("bar"))
    Await.result(session.del("foo"))
    Await.result(session.get("foo")) should be (None)
  }


}
