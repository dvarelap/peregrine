# peregrine

[![Join the chat at https://gitter.im/dvarelap/peregrine](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/dvarelap/peregrine?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/dvarelap/peregrine.svg)](https://travis-ci.org/dvarelap/peregrine)

## Current Version - 1.0.1

**peregrine** is a fast & thin Scala web framework inspired by Sinatra and powered by Twitter-Server

***Note:*** this is a framework based on finatra 1.x.x. Why?, well since finatra 2.0.0-M1 it's not simple as it should be to write a simple and fast web app, so I decided to take a different path based on this awesome project and keep it the Sinatra way.

Here a small app
```scala
import com.github.dvarelap.peregrine._

class HelloController extends Controller {

  get("/hello/:name") { request =>
    val name = request.routeParams.getOrElse("name", "nothing")
    render.plain("hello " + name).toFuture
  }
}

object MyServer extends PeregrineServer {
  register(new HelloController)
}
```

## Quick Start
See full documentation [here](docs.md)


### Setup

To get started, we have to define at least one `Controller` and register it with `PeregrineServer`, like so:

```scala
class Example extends Controller {
  get("/") { request =>
    render.plain("hi").toFuture
  }
}

class MyServer extends PeregrineServer {
  val controller = new Example()
  register(controller)
}
```

### SBT

```scala
// Resolver
resolvers += "dvarelap repo" at "http://dl.bintray.com/dvarelap/maven"

// Dependency
"com.github.dvarelap" %% "peregrine" % "1.0.1"
 ```

## Want to join the trip?
Fork the project, right away!
and code/fix one of [these issues](https://github.com/dvarelap/peregrine/issues)


## Licence
Copyright 2015 Daniel Varela and other contributors.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
