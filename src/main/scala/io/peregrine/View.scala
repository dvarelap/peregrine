package io.peregrine

trait Csrf {
  def csrfToken: Option[String]               = _csrf
  private[peregrine] var _csrf:Option[String] = None
}

case class View(format: String,
                template: String,
                model: Any,
                contentType: Option[String] = None,
                message: Option[String] = None,
                error: Option[String] = None) extends Csrf
