package com.github.dvarelap.stilt

trait JsonSerializer {
  def serialize[T](item: T): Array[Byte]
}