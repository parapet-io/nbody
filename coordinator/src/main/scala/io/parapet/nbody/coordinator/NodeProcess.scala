package io.parapet.nbody.coordinator

import cats.effect.IO
import io.parapet.cluster.node.{MessageHandler, Node, Req}
import io.parapet.core.Event._
import io.parapet.core.{DslInterpreter, Process, ProcessRef}

class NodeProcess(config: Config, sink: ProcessRef) extends Process[IO] {

  import dsl._

  private val msgHandler = new MessageHandler() {
    override def handle(req: Req): Unit = {
      (req ~> sink).foldMap(DslInterpreter.instance.interpret(ref, sink)).unsafeRunSync() // temporary workaround
    }
  }

  private val node =
    new Node(host = config.host, port = config.port,
      id = "coordinator",
      servers = config.servers.split(","),
      msgHandler = msgHandler)

  override def handle: Receive = {
    case Start => eval {
      node.connect()
      node.join("nbody")
    }
    case req: Req => eval(node.send(req))
  }

}
