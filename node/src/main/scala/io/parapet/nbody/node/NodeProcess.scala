package io.parapet.nbody.node

import cats.effect.IO
import com.typesafe.scalalogging.Logger
import io.parapet.cluster.node.{MessageHandler, Node, Req}
import io.parapet.core.Event._
import io.parapet.core.{DslInterpreter, Process, ProcessRef}

class NodeProcess(config: Config, sink: ProcessRef) extends Process[IO] {

  import dsl._

  private val logger = Logger[NodeProcess]

  override val ref: ProcessRef = Constants.NodeRef

  private val msgHandler = new MessageHandler() {
    override def handle(req: Req): Unit = {
      (req ~> sink).foldMap(DslInterpreter.instance.interpret(ref, sink)).unsafeRunSync() // temporary workaround
    }
  }

  private val node =
    new Node(host = config.host, port = config.port,
      id = s"node-${config.nodeId}",
      servers = config.servers.split(","),
      msgHandler = msgHandler)

  override def handle: Receive = {
    case Start => eval {
      node.connect()
      node.join("nbody")
      logger.info("joined cluster")
    }
    case req: Req => eval {
      logger.debug("received req from upstream")
      node.send(req)
    }
  }

}
