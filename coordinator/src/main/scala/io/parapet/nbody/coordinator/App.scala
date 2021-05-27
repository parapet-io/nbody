package io.parapet.nbody.coordinator

import cats.effect.IO
import io.parapet.{CatsApp, core}

object App extends CatsApp{

  override def processes(args: Array[String]): IO[Seq[core.Process[IO]]] = {
    IO {
      val config = Config(args)
      val nodeProcess = new NodeProcess(config,Constants.CoordinatorRef)
      val coordinatorProcess = new CoordinatorProcess(config)
      Seq(nodeProcess, coordinatorProcess)
    }
  }
}