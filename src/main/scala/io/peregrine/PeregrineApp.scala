package io.peregrine

import com.twitter.server.TwitterServer

trait PeregrineApp extends PeregrineServer with Controller { register(this) }
