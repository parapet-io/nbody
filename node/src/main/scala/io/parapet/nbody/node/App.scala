package io.parapet.nbody.node

import cats.effect.IO
import io.parapet.{CatsApp, core}

object App extends CatsApp {
  override def processes(args: Array[String]): IO[Seq[core.Process[IO]]] = {
    IO {
      val config = Config(args)
      val nBodySystemProcess = new NBodySystemProcess(config)
      val nodeProcess = new NodeProcess(config, Constants.NBodySystemRef)
      Seq(nodeProcess, nBodySystemProcess)
    }
  }
}
