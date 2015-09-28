# peregrine

[![Join the chat at https://gitter.im/dvarelap/peregrine](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/dvarelap/peregrine?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/dvarelap/peregrine.svg)](https://travis-ci.org/dvarelap/peregrine)
[![Bintray](https://img.shields.io/bintray/v/dvarelap/maven/peregrine.svg)]()

Minimal app:
```scala
import io.peregrine._

object WebApp extends PeregrineApp {
  get("/hi") { req =>
    render.plain("Hello World!").toFuture
  }
}
```

Install dependency in `build.sbt` file:
```scala
scalaVersion := "2.11.7"
resolvers += "Twitter" at "http://maven.twttr.com"
libraryDependencies += "com.github.dvarelap" %% "peregrine" % "1.1.2"
```

And run with:
```batch
$ sbt run
```

View at: [http://localhost:5000/hi](http://localhost:5000/hi)

## Full Documentation
See full documentation [here](docs.md)

**Note** Peregrine works only with scala version 2.11+

## Licence
Copyright 2015 Daniel Varela and other contributors.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
