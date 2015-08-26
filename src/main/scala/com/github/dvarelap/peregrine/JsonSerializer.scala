package com.github.dvarelap.peregrine

trait JsonSerializer {
  def serialize[T](item: T): Array[Byte]
}