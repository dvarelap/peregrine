package io.peregrine

class ErrorView(ex: Throwable) extends View {
  def render = s"""
    ${ex.toString()}
  """
}
