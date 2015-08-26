package com.github.dvarelap.peregrine.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dvarelap.peregrine.JsonSerializer
import org.jboss.netty.util.CharsetUtil._
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
 * Jackson implementation of a JsonSerializer with the ScalaModule added
 * by default.
 */
class JacksonJsonSerializer(val mapper: ObjectMapper) extends JsonSerializer {
  def serialize[T](item: T) = mapper.writeValueAsString(item).getBytes(UTF_8)
}

object DefaultJacksonJsonSerializer
  extends JacksonJsonSerializer(new ObjectMapper().registerModule(new DefaultScalaModule))