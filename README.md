# peregrine

(this is a forked version of Peregrine with some modifications to support
plugins for the `PeregrineServer`)

Minimal app:
```scala
import io.peregrine._

object WebApp extends PeregrineApp {
  get("/hi") { req =>
    "Hello World!"
  }
}
```

Install dependency in `build.sbt` file:
```scala
scalaVersion := "2.11.7"
resolvers += "Twitter" at "http://maven.twttr.com"
resolvers += "bintray" at "https://dl.bintray.com/dvarelap/maven"
libraryDependencies += "com.github.dvarelap" %% "peregrine" % "1.2.2"
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
