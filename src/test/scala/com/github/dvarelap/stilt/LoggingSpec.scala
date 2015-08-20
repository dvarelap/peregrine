package com.github.dvarelap.stilt

class LoggingSpec extends ShouldSpec {

  // TODO need to fix this test
//  trait TestApp extends App with Logging
//
//  "logLevel" should "be INFO by default" in {
//    new TestApp {
//      LevelFlaggable.parse(logLevel()) should equal(Level.INFO)
//    }
//  }
//
//  "flag settings" should "work" in {
//    System.setProperty("com.github.dvarelap.stilt.config.logLevel", "DEBUG")
//    LevelFlaggable.parse(logLevel()) should equal(Level.INFO)
//  }
//
//  "logLevel" should "respect flag settings" in {
//    new TestApp {
//      System.setProperty("com.github.dvarelap.stilt.config.logLevel", "DEBUG")
//      LevelFlaggable.parse(logLevel()) should equal(Some(Level.DEBUG))
//    }
//  }
//
//  "logLevel" should "throw on a wrong level name" in {
//    System.setProperty("com.github.dvarelap.stilt.config.logLevel", "Blah")
//    a[IllegalArgumentException] should be thrownBy {
//      new TestApp {
//        val foo = LevelFlaggable.parse(logLevel())
//      }
//    }
//  }
}
