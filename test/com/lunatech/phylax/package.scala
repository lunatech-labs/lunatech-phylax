package com.lunatech

import java.io.File

import com.lunatech.phylax.controllers.PhylaxComponents
import org.scalatest.TestData
import play.api.{Application, ApplicationLoader, Environment, Mode}

package object phylax {
  def startAppForTest(testData: TestData): Application = {
    val classLoader = ApplicationLoader.getClass.getClassLoader
    val env = new Environment(new File("."), classLoader, Mode.Test)
    new PhylaxComponents(ApplicationLoader.createContext(env)).application
  }
}
