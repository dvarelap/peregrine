package com.github.dvarelap.stilt

class RouteVector[A] {
  var vector = Vector[A]()

  def add(x: A) {
    vector = x +: vector
  }
}
