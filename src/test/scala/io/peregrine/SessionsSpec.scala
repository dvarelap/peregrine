package io.peregrine

import io.peregrine.test.FlatSpecHelper
import com.twitter.finagle.http.Cookie

class SessionsSpec extends ShouldSpec with FlatSpecHelper {

  override def server: PeregrineServer = new PeregrineServer {
    register(TestControllerWithSessions)
  }

  // default behaviour
  object TestControllerWithSessions extends Controller with Sessions {
    override def createSessionCookie                = super.createSessionCookie
    override def cookieBuilder                      = super.cookieBuilder
    override def buildSessionCookie(value: String)  = super.buildSessionCookie(value)

    get("/put_value") { req =>
      for {
        session <- session(req)
        _       <- session.put("foo", "bar")
      } yield render.ok
    }

    get("/get_value") { req =>
      for {
        session <- session(req)
        value   <- session.get[String]("foo")
      } yield render.plain(value.getOrElse("error"))
    }

    get("/get_put_value") { req =>
      for {
        session <- session(req)
        value   <- session.getOrElseUpdate[String]("foo", "boo")
      } yield render.plain(value)
    }
  }

  "#createSessionCookie" should "create a cookie with _session_id and a random number as value" in {
    val sessionCookie: Cookie = TestControllerWithSessions.createSessionCookie
    sessionCookie.name      should be ("_session_id")
    sessionCookie.value     should not be ""
    sessionCookie.httpOnly  should be (right = true)
  }

  "#buildSessionCookie" should "create a cookie with _session_id and the value passed as parameter" in {
    val cookie = TestControllerWithSessions.buildSessionCookie("TEST_COOKIE_VALUE")
    cookie.name      should be ("_session_id")
    cookie.value     should be ("TEST_COOKIE_VALUE")
    cookie.httpOnly  should be (right = true)
  }

  "#session" should "return a valid session and value within session" in  {
    get("/put_value")
    response.body   should be ("")
    val maybeCookie = response.originalResponse.cookies.get("_session_id")
    maybeCookie     should not be None
    val cookie      = maybeCookie.get
    cookie.value    should not be ""
    cookie.name     should be ("_session_id")

    get("/get_value", headers = Map("Cookie" -> s"_session_id=${cookie.value};"))
    response.body should be ("bar")
  }

  "#session" should "put and return a valid session and value within session" in  {
    get("/get_put_value")
    response.body   should be ("boo")
    val maybeCookie = response.originalResponse.cookies.get("_session_id")
    maybeCookie     should not be None
    val cookie      = maybeCookie.get
    cookie.value    should not be ""
    cookie.name     should be ("_session_id")

    get("/get_value", headers = Map("Cookie" -> s"_session_id=${cookie.value};"))
    response.body should be ("boo")
  }
}
