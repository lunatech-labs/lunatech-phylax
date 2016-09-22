package com.lunatech.phylax.controllers

import java.time.Clock

import com.lunatech.phylax.filters.ExampleFilter
import com.lunatech.phylax.services.{ApplicationTimer, AtomicCounter}
import play.api._
import play.api.ApplicationLoader.Context
import play.api.http.DefaultHttpErrorHandler
import play.api.routing.Router
import router.Routes
import play.api.mvc.EssentialFilter
import play.api.libs.concurrent.Execution.Implicits._

class PhylaxApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }

    new PhylaxComponents(context).application
  }
}

class PhylaxComponents(context: Context) extends BuiltInComponentsFromContext(context) {
  override def router: Router = new Routes(DefaultHttpErrorHandler,
    new HomeController,
    new CountController(new AtomicCounter),
    new AsyncController(actorSystem),
    new _root_.controllers.Assets((DefaultHttpErrorHandler))
  )

  val applicationTimer = new ApplicationTimer(Clock.systemDefaultZone(), applicationLifecycle)

  override lazy val httpFilters: Seq[EssentialFilter] =
    if (context.environment.mode == Mode.Dev)
      Seq(new ExampleFilter)
    else
      Nil
}
