package com.lunatech.phylax

import org.scalatest.{Suite, TestData}
import org.scalatestplus.play.OneServerPerTest
import play.api.Application

trait PhylaxOneServerPerTest extends OneServerPerTest { self: Suite =>
  override def newAppForTest(testData: TestData): Application = startAppForTest(testData)
}
