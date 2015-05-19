# Stilt

[![Join the chat at https://gitter.im/dvarelap/stilt](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/dvarelap/stilt?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Current Version - 0.0.1

**Stilt** is a fast & thin Scala web framework inspired by Sinatra and powered by Twitter-Server

***Note:*** this is a framework based on finatra 1.x.x. Why?, well since finatra 2.0.0-M1 it's not simple as it should be to write a simple and fast web app, so I decided to take a different path based on this awesome project and keep it the Sinatra way.
      
Here a small app
```scala
import com.github.dvarelap.stilt._

class HelloController extends Controller {

  get("/hello/:name") { request =>
    val name = request.routeParams.getOrElse("name", "nothing")
    render.plain("hello " + name).toFuture
  }
}

object MyServer extends StiltServer {
  register(new HelloController())
}
```

## Quick Start
### SBT 
```scala
 "com.github.dvarelap" %% "stilt" % "0.0.1"
 ```
### Maven
```xml
<dependency>
  <groupId>com.github.dvarelap</groupId>
  <artifactId>stilt_2.11</artifactId>
  <version>0.0.1</version>
</dependency>
```
## What is gone?

The default view to mustache, no we'll leave this an open desicion to the developer.

TODO: sample here.

## What should be implemented?
- Session support
- CSRF protection

## Want to join the trip? 


## Licence 

Copyright 2015 Daniel Varela and other contributors.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0