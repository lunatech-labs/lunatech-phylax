package com.lunatech.phylax

import org.scalatest.{Suite, TestData}
import org.scalatestplus.play.OneAppPerTest
import play.api.Application

trait PhylaxOneAppPerTest extends OneAppPerTest { self: Suite =>
  override def newAppForTest(testData: TestData): Application = startAppForTest(testData)
}
