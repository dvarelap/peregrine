package io.peregrine

object ErrorHandler {

  def apply(request: Request, e: Throwable, controllers: ControllerCollection) = {
    request.error = Some(e)
    ResponseAdapter(request, controllers.errorHandler(request))
  }
}