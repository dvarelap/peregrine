package io.peregrine

import org.scalatest.{FlatSpec, Matchers}

class ShouldSpec extends FlatSpec with Matchers {
  def removeWhiteSpaces(input: String): String = {
    input.stripMargin.trim.replaceAll("""(?m)\s+$""", "")
  }
}
