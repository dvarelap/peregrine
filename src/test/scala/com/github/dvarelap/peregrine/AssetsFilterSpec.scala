package com.github.dvarelap.peregrine

import com.github.dvarelap.peregrine.test.FlatSpecHelper
import com.twitter.finagle.http.Cookie

class AssetsFilterSpec extends ShouldSpec with FlatSpecHelper {

  override def server: PeregrineServer = new PeregrineServer {
    addFilter(new AssetsFilter(assetsPrefix = "/assets/"))
    register(testController)
  }

  object testController extends Controller {
    get("/assets/:path") { req =>
      render.plain("should not reach this").toFuture
    }
  }

  "GET /assets/" should "not show action and reneder the correct asset" in {
    get("/assets/test.css")
    response.code      should equal(200)
    response.body.trim should equal("""body {background: #FFF;}""")
  }

  "GET /assets/path-with-params" should "not show action and reneder the correct asset" in {
    get("/assets/test.css?q=1&s=fido")
    response.code      should equal(200)
    response.body.trim should equal("""body {background: #FFF;}""")
  }

  "GET /assets/subfolders" should "not show action and reneder the correct asset" in {
    get("/assets/components/test_folder/subfoldertest.js?q=1&s=fido")
    response.code      should equal(200)
    response.body.trim should equal("""function (e) {return e + 'a';}""")
  }
}
