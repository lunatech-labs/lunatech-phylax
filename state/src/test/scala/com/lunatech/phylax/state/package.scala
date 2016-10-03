package com.lunatech.phylax

import com.typesafe.config.{Config, ConfigFactory}

package object state {

  def testConfig: Config =
    ConfigFactory.load("application.conf")//.
//      withFallback(ConfigFactory.defaultApplication()).
//      withFallback(ConfigFactory.load("reference.conf"))
}
