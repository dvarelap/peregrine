package io.peregrine

import com.twitter.finagle.http.Cookie
import com.twitter.util.Future

import scala.collection.mutable

trait Sessions extends BaseSessions {

  // needs to be overridden in order to create a different session type
  protected def createSession[S >: Session](sessionId: String) = new CookieBasedSession(sessionId)

  // needs to be overridden in order to create a different session type
  protected def getOrCreateSession(sessionId: String): Session = {
    CookieSessionsHolder.getOrElseUpdate(sessionId, createSession(sessionId))
  }
}

trait BaseSessions {

  protected def createSessionCookie: Cookie = {
    buildSessionCookie(IdGenerator.hexString(64))
  }

  protected def cookieBuilder: CookieBuilder = {
    new CookieBuilder
  }

  protected def buildSessionCookie(value: String): Cookie = {
    cookieBuilder.name("_session_id")
      .value(value)
      .httpOnly(httpOnly = true)
      // enables cookies for secure session if cert and key are provided
      .secure(!config.certificatePath().isEmpty && !config.keyPath().isEmpty)
      .build()
  }

  // needs to be overridden in order to create a different session type
  def session(implicit req: Request): Future[Session] = Future {
    req.cookies.get("_session_id") match {
      case Some(cookie) =>
        req.response.addCookie(buildSessionCookie(cookie.value))
        getOrCreateSession(cookie.value)
      case None         =>
        val cookie = createSessionCookie
        req.response.addCookie(cookie)
        getOrCreateSession(cookie.value)
    }
  }

  // needs to be overridden in order to create a different session type
  protected def createSession[S >: Session](sessionId: String): S

  // needs to be overridden in order to create a different session type
  protected def getOrCreateSession(sessionId: String): Session
}

trait Session {
  type Seconds = Long

  def get[T](key: String): Future[Option[T]]
  def put[T](key: String, value: T, expiresIn: Seconds = 3600000): Future[Unit]
  def del(key: String): Future[Unit]
  def getOrElseUpdate[T](key: String, value: T): Future[T]
}

class CookieBasedSession(val sessionId: String) extends Session {

  private val values = mutable.Map[String, Any]()

  override def get[T](key: String): Future[Option[T]] = Future(values.get(key).map(_.asInstanceOf[T]))
  override def put[T](key: String, value: T, expiresIn: Seconds = 3600000): Future[Unit] = Future(values.put(key, value))
  override def del(key: String): Future[Unit] = Future(values.remove(key))
  override def getOrElseUpdate[T](key: String, value: T):Future[T] = get[T](key).flatMap {
    case Some(t) => Future(t)
    case None    => put[T](key, value).map(_ => value)
  }
}

private[this] object CookieSessionsHolder {

  private val sessions = mutable.Map[String, CookieBasedSession]()

  def get(id: String): Option[Session] = sessions.get(id)
  def put(id: String, session: CookieBasedSession): Unit = sessions.put(id, session)
  def del(id: String): Unit = sessions.remove(id)
  def getOrElseUpdate(id: String, session: CookieBasedSession) = sessions.getOrElseUpdate(id, session)
}

private[peregrine] object IdGenerator {

  import java.security.SecureRandom

  private val secureRandom = new SecureRandom()

  def hexString(bytes: Int): String = {
    val data = new Array[Byte](bytes)
    secureRandom.nextBytes(data)
    data.map("%02x" format _).mkString
  }
}
