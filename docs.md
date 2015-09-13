# Peregrine Documentation

*   [Getting Started](#Getting Started)
*   [Routing](#Routing)
*   [Params](#Params)
*   [Request](#Request)
*   [Controllers](#Controllers)
*   [Futures](#Futures)
*   [Responses](#Responses)
*   [Assets](#Assets)
*   [Headers](#Headers)
*   [Cookies](#Cookies)
*   [Uploads](#Uploads)
*   [Filters](#Filters)
*   [Logging](#Logging)
*   [Stats](#Stats)
*   [Testing](#Testing)
*   [Deploying](#deploying)



<!-- *   [Templates](#templates) -->


## Getting Started

Minimal app:
```scala
import io.peregrine._

object WebApp extends PeregrineApp {
  get("/hi") { req =>
    render.plain("Hello World!").toFuture
  }
}
```

Install dependency in build.sbt file:
```scala
resolvers += "dvarelap repo" at "http://dl.bintray.com/dvarelap/maven"

libraryDependencies += "com.github.dvarelap" %% "peregrine" % "1.1.0"
```

And run with:
```batch
$ sbt run
```

View at: [http://localhost:5000/hi](http://localhost:5000/hi)

## Routing

A route is a HTTP method paired with a URL matching pattern.
When Peregrine receives a request for a particular URL, it will scan all registered routes and dispatch to the first one that contains a match.

```scala
get("/") { req =>
// ...
}

delete("/") { req =>
// ...
}

post("/") { req =>
// ...
}

put("/") { req =>
// ...
}

head("/") { req =>
// ...
}

patch("/") { req =>
// ...
}

options("/") { req =>
// ...
}
```

It's also possible render a route from another route, like:

```scala
get("/") { request =>
  route.get("/home")
}

get("/home") { request =>
  ...
}
```

And you can define multiple routes for a single action like:

```scala
// GET /home & GET /inicio will do the exact same action
get("/home", "/inicio") { request =>
  ...
}
```

You can even pass params through:

```scala
get("/dog-search") { request =>
  route.post("/search", Map("q" -> "dogs"))
}
```

Customize what happens when a route isn't found using `notFound`:

```scala
notFound { request =>
  render.status(404).plain("not found yo").toFuture
}
```

Or, what happens when exceptions occur with `error:`

```scala
class Unauthorized extends Exception

error { request =>
    request.error match {
      case Some(e:ArithmeticException) =>
        render.status(500).plain("whoops, divide by zero!").toFuture
      case Some(e:Unauthorized) =>
        render.status(401).plain("Not Authorized!").toFuture
      case Some(e:UnsupportedMediaType) =>
        render.status(415).plain("Unsupported Media Type!").toFuture
      case _ =>
        render.status(500).plain("Something went wrong!").toFuture
    }
}

get("/secret") { request =>
  throw(new Unauthorized)
}
```

## Params

Query parameters are supported through `request.params`. This supports all the usual `Map` methods you are used to, like `getOrElse`:

```scala
get("/search") { request =>
  val query = request.params.getOrElse("q", "dogs")
  render.plain("you searched for " + query).toFuture
}
```

Parameters can also be extracted from routes just like in [Sinatra](http://sinatrarb.com). These are stored in `request.routeParams`:

```scala
get("/hello/:name") { request =>
  val name = request.routeParams.getOrElse("name", "john doe")
  render.plain("you searced for " + query).toFuture
}
```

And a most generic way to extract params is via `param` method
```scala

case class Person(firstName: String, lastName: String)

// GET /hello/Dan?lastName=Varela
get("/hello/:firstName") { req =>
  val u = for {
    firstName <- req.param("firstName")
    lastName  <- req.param("lastName")
  } yield Person(firstName, lastName)

  render.json(u).toFuture
}
```

## Request

You'll notice a `request` object is passed into your routing code, this has useful information about the request:

```scala
get("/request-info") { request =>
  println(request.remoteAddress)
  println(request.path)
  println(request.userAgent)
  render.plain("done").toFuture
}
```

## Controllers

If you need to organize the actions in different files you can extends `Controller` instead:

```scala
class UsersController extends Controller {
  get("/") { request =>
    // ...
  }
  post("/") { request =>
    // ...
  }
  delete("/:id") { request =>
    // ...
  }
}
```

and then register it in the `PeregringeApp`

```scala
class MyServer extends PeregrineApp {
  val usersController = new UsersController()
  register(usersController)
}
```
You can call `register` multiple times to register various controllers.

You can also define a prefix for each controller you register:
```scala
class UsersController extends Controller {
  get("/") { request =>
    // ...
  }
}

class CompanyController extends Controller {
  get("/") { request =>
    // ...
  }
}


object MyPrefixServer extends PeregrineApp {

  register(new UsersController, "/users") // will respond on GET /users/
  register(new CompanyController, "/companies") // will respond on GET /companies/
}
```

## Futures

Every route is expected to a return a `Future[Response]`, hence all the `.toFuture` calls you've been seeing in our examples. This is an important distinction from synchronous frameworks as all your routes may be executed concurrently instead of one at a time. It's especially useful when dealing with libraries/services that return `Future`'s themselves (like a finagle-http client):

```scala
get("/current-time") { request =>
  httpClient.apply("/api/time.txt") map { response =>
    val currentTime = response.contentString()
    render.plain("the time is: " + currentTime)
  }
}
```

Note that we did not use `.toFuture` above because we are already within a `Future`.

See [Concurrent Programming with Futures](http://twitter.github.io/finagle/guide/Futures.html) for more details.


## Responses

The `render` object is a powerful `Response` builder that allows customizing the response in various ways:

```scala
get("/i-want-json") { request =>
  render.json(Map("foo" -> "bar")).toFuture
}
```

This will automatically set the `Content-Type` as `application/json`.

```scala
get("/i-want-html") { request =>
  render.html("<h1>hi</h1>").toFuture
}
```

Like the example above, this sets `Content-Type` to `text/html`. We can also set it to whatever we want:

```scala
get("/i-want-html") { request =>
  render.body("custom response").contentType("application/mine").toFuture
}
```

Because it's a builder, you can chain the methods in any order. Let's add a `201` to that response:

```scala
get("/i-want-html") { request =>
  render.body("custom response")
        .contentType("application/mine")
        .status(201)
        .toFuture
}
```

This is the same as:

```scala
get("/i-want-custom") { request =>
  render.status(201)
        .contentType("application/mine")
        .body("custom response")
        .toFuture
}
```

Sending a byte array:

```scala
get("/i-want-binary") { request =>
  render.status(201)
        .contentType("application/octet-stream")
        .body(Array[Byte](12, 41, 51))
        .toFuture
}
```

It's also possible to respond conditionally based on `Content-Type` or `Accept` header:

```scala
get("/api/thing") { request =>
  respondTo(request) {
    case _:Html => render.html("<p>html response</p>").toFuture
    case _:Json => render.json(Map("value" -> "an json response")).toFuture
    case _:All => render.plain("default fallback response").toFuture
  }
}
```

See the `Response` class for more details.




<!-- # Templates

Mustache is natively supported through [Mustache.java](https://github.com/spullara/mustache.java). First a `[View](https://github.com/twitter/peregrine/blob/1.5.3/src/main/scala/com/twitter/peregrine/View.scala)` class must be defined:

```scala
class MyView extends View {
  val template = "my_view.mustache"
  val some_val = "random value here"
}

```

By default, this will look in `src/main/resources/templates/my_view.mustache` for the template source, which could be:

```scala
 <h1>Some value is {{some_val}}</h1>

```

Then `render.view` can be used to display it:

```scala
get("/template") { request =>
  val myView = new MyView
  render.view(myView).toFuture
}
```

You can imagine more complex views taking constructor arguments and doing things conditionally from request input.

 -->


## Assets

Theres an embedded static file server which will serve out of `src/main/resources/public` by default. It's also possible to render assets inside of routes:

```scala
get("/deal-with-it") { request =>
  render.static("/dealwithit.gif")
}
```

It's important to note that the `Router` runs _before_ the file server, allowing you to dynamically intercept static assets:

```scala
get("/file.txt") { request =>
  render.plain("this is the file").toFuture
}
```




## Headers

To read headers, use `request.headerMap`; much like `request.params`, this is also a `Map`

```scala
get("/") { request =>
  val isFoo = request.headerMap.getOrElse("X-Foo", "1")
  render.plain("X-Foo status: " + isFoo).toFuture
}
```

Setting headers is available on the `Response` builder:

```scala
get("/") { request =>
  render.plain("hi").header("Foo", "Bar").toFuture
}
```

You can call `header` multiple times or pass a map to `headers`:

```scala
get("/") { request =>
  render.plain("hi")
        .header("Foo", "Bar")
        .header("Biz", "Baz")
        .toFuture
}
```

```scala
get("/") { request =>
  render.plain("hi")
        .headers(Map("Foo" -> "Bar", "Biz" -> "Baz"))
        .toFuture
}
```




## Cookies

Cookies, like `Headers`, are read from `request` and set via `render`:

```scala
get("/") { request =>
  val loggedIn = request.cookie("loggedIn").getOrElse("false")
  render.plain("logged in?:" + loggedIn).toFuture
}
```

```scala
get("/") { request =>
  render.plain("hi")
        .cookie("loggedIn", "true")
        .toFuture
}
```

Advanced cookies are supported by creating and configuring `Cookie` objects:

```scala
get("/") { request =>
  val c = DefaultCookie("Biz", "Baz")
  c.setSecure(true)
  render.plain("get:path").cookie(c).toFuture
}
```

See the `Cookie` class for more details.




## Uploads

Uploads are fully supported in the `request.multiParams` object.

```scala
post("/profile") { request =>
  request.multiParams.get("avatar").map { avatar =>
    println("content type is " + avatar.contentType)
    avatar.writeToFile("/tmp/avatar")
  }
  render.plain("ok").toFuture
}
```

See the `MultipartItem` class for more details.




## Filters

Filters are code that runs before any request is dispatched to a particular `Controller`. They can modify the incoming request as well as the outbound response. A great example is our own `LogginFilter`:

```scala
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import com.twitter.finagle.http.{Request => FinagleRequest
import com.twitter.finagle.http.{Response => FinagleResponse}
import com.twitter.app.App

class LoggingFilter
  extends SimpleFilter[FinagleRequest, FinagleResponse] with App with Logging  {

  def apply(
    request: FinagleRequest,
    service: Service[FinagleRequest, FinagleResponse])
  ) = {
    val start = System.currentTimeMillis()
    service(request) map { response =>
      val end = System.currentTimeMillis()
      val duration = end - start
      log.info("%s %s %d %dms".format(request.method,
                                      request.uri,
                                      response.statusCode,
                                      duration))
      response
    }
  }
}

```

You can register these inside `Peregrine` like so:

```scala
class MyServer extends PeregrineApp
  addFilter(new SimpleFilter)
  register(new ExampleController)
end

```




## Logging

There is a `log` log object available inside every `Controller` with the standard error levels (info, warn, error, etc):

```scala
post("/profile") { request =>
  try {
    fetchProfileFromJankyServer()
  } catch {
    case exception => log.error(exception, "something bad happened")
  }
  log.info("sending ok")
  render.plain("ok").toFuture
}
```




## Stats

Theres also a default `[StatsReceiver](https://github.com/twitter/finagle/blob/master/finagle-core/src/main/scala/com/twitter/finagle/stats/StatsReceiver.scala)` object available for recording metrics named `stats`:

```scala
post("/profile") { request =>
  try {
    stats.counter("profile/attempts").incr
    stats.time("profile/fetch") {
      fetchProfileFromJankyServer()
    }
  } catch {
    stats.counter("profile/fails").incr
    case exception => log.error(exception, "something bad happened")
  }
  log.info("sending ok")
  render.plain("ok").toFuture
}
```

These can be collected by visiting `/admin/metrics.json` on the admin port, which is `:9990` by default.

See the [HTTP Admin Interface](http://twitter.github.io/twitter-server/Features.html#http-admin-interface) page of Twitter Server for more details.




## Testing

You can unit test your controllers using the MockApp helper:

```scala
class SampleController extends Controller {
  get("/testing") {
    request => render.plain("hi").toFuture
  }
}

"GET /testing" should "be 200" in {
  val app = MockApp(new SampleController)
  val response = app.get("/testing")

  response.code should be(200)
  response.body should be("hi")
}

```

Alternatively, you can use SpecHelper trait to test the complete App:

```scala
class AppSpec extends FlatSpecHelper {

  val app = new App.ExampleApp

  "GET /" should "respond 200 with hi" in {
    get("/")
    response.body should equal ("hi")
    response.code should equal (200)
  }
}
```




# Deploying

To generate a deployable single jar "fatjar", you can use [sbt-assembly](https://github.com/sbt/sbt-assembly)
add the following to `project/plugings.sbt` (if the folder doesn't exists, go ahead an create it)
```scala
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.0")
```

and then

```scala
sbt assembly
```

This produces a runnable jar with scala, peregrine, and any other dependent libraries included inside the `target/` directory.

If you are using Heroku, create a `[Procfile](https://github.com/twitter/peregrine/blob/1.5.3/script/peregrine/share/Procfile)` like
```
web:    java -Dio.peregrine.config.env=production -Dio.peregrine.config.adminPort='' -Dio.peregrine.config.port=:$PORT -cp target/classes:target/dependency/* app
```

and it will work out of the box.
