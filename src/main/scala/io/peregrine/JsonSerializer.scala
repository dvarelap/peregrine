package io.peregrine

trait JsonSerializer {
  def serialize[T](item: T): Array[Byte]
}