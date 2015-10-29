package io.peregrine

// import java.util.concurrent.Callable
// //
// // trait View extends Callable[String] with ViewCsrf {
// //   def render: String
// //   def call: String                = render
// //   def contentType: Option[String] = None
// // }
// //
// // trait ViewCsrf {
// //   def csrfToken: Option[String]     = _csrf
// //   private[peregrine] var _csrf:Option[String] = None
// // }

trait Csrf {
  def csrfToken: Option[String]               = _csrf
  private[peregrine] var _csrf:Option[String] = None
}


case class View(format: String,
                template: String,
                model: Any,
                contentType: Option[String] = None) extends Csrf
