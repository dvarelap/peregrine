package io.peregrine.view

import io.peregrine._

class MustacheViewRendererSpec extends ShouldSpec {
  val resourceRender = new LocalTemplateMustacheViewRenderer {
    override val templateRoot = "/src/test/resources/"
  }

  "#format" should "be 'mustache'" in {
    resourceRender.format should equal("mustache")
  }

  "#render" should "form a correct template based on a string" in {
    val result = resourceRender.render("test_str", View("mustache", "test_str", "STRING_TEST"))
    removeWhiteSpaces(result) should equal("Result expected: STRING_TEST")
  }

  "#render" should "form a correct template based on an object" in {
    case class User(name: String, age: Int, opt: Option[String] = None)

    val u1 = User("John", 12, None)
    val result = resourceRender.render("test_obj", View("mustache", "test_obj", u1))
    removeWhiteSpaces(result) should equal(
     """Name: John
        |Age: 12
        |Opt:"""stripMargin
    )

    val u2 = User("Frank", 21, Some("wow"))
    val result2 = resourceRender.render("test_obj", View("mustache", "test_obj", u2))
    removeWhiteSpaces(result2) should equal(
      """Name: Frank
        |Age: 21
        |Opt: wow
      """.stripMargin.trim)
  }

  "#render" should "form a correct template based on an collection" in {
    case class User(name: String, age: Int, opt: Option[String] = None)

    val seq = Seq(
      User("John", 12, None),
      User("Frank", 21, Some("wow"))
    )
    val result = resourceRender.render("test_seq", View("mustache", "test_seq", seq))
    removeWhiteSpaces(result) should equal(
      """Name: John
        |  Age: 12
        |  Opt:
        |  Name: Frank
        |  Age: 21
        |  Opt: wow
      """.stripMargin.trim)
  }

}
