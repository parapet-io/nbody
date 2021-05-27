package io.parapet.nbody.coordinator

import cats.effect.IO
import io.parapet.cluster.node.Req
import io.parapet.core.Dsl.DslF
import io.parapet.core.{Process, ProcessRef}
import io.parapet.nbody.api.Nbody
import io.parapet.nbody.api.Nbody.{Cmd, CmdType, Update}
import io.parapet.nbody.core.Body

class CoordinatorProcess(config: Config) extends Process[IO] {

  import dsl._

  private val nodeIds = (1 to config.nodeSize).map(i => s"node-$i").toSet

  private val bodies = Array.fill[Body](config.nbodySize) {
    new Body
  }

  override val ref: ProcessRef = Constants.CoordinatorRef

  private var updateCount = 0

  override def handle: Receive = {
    case Req(_, data) =>
      val cmd = Cmd.parseFrom(data)
      cmd.getCmdType match {
        case CmdType.UPDATE =>
          eval {
            val update = Update.parseFrom(cmd.getData)
            var j = update.getFrom
            for (i <- 0 until update.getBodyCount) {
              val body = update.getBody(i)
              bodies(j).x = body.getX
              bodies(j).y = body.getY
              bodies(j).z = body.getZ

              bodies(j).vx = body.getVx
              bodies(j).vy = body.getVy
              bodies(j).vz = body.getVz

              bodies(j).mass = body.getMass

              j = j + 1
            }
            updateCount = updateCount + 1
          } ++ flow {
            if (updateCount == config.nodeSize) {
              val updateBuilder = Nbody.Update.newBuilder()
              bodies.foreach(body =>
                updateBuilder.addBody(Nbody.Body.newBuilder()
                  .setX(body.x)
                  .setY(body.y)
                  .setZ(body.z)
                  .setVx(body.vx)
                  .setVy(body.vy)
                  .setVz(body.vz)
                  .setMass(body.mass).build()))
              val update = updateBuilder.setFrom(0).setTo(bodies.length).build()
              logBodies ++
                nodeIds.foldLeft(unit)((acc, nodeId) => acc ++ Req(nodeId, update.toByteArray) ~> Constants.NodeRef) ++
                nodeIds.foldLeft(unit)((acc, nodeId) =>
                  acc ++ Req(nodeId, Cmd.newBuilder().setCmdType(CmdType.NEXT_ROUND).build().toByteArray) ~> Constants.NodeRef)
            } else eval {
              println(s"received updates from $updateCount nodes")
            }
          }
      }
  }

  def logBodies: DslF[IO, Unit] = {
    eval {
      for (i <- bodies.indices) {
        val body = bodies(i)
        println(s"body[$i], x=${body.x}, y=${body.y}, z=${body.z}, vx=${body.vx}, vy=${body.vy}, vz=${body.vz}, mass=${body.mass}")
      }
    }
  }
}
