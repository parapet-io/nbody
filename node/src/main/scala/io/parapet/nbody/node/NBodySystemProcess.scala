package io.parapet.nbody.node

import cats.effect.IO
import com.typesafe.scalalogging.Logger
import io.parapet.cluster.node.Req
import io.parapet.core.Dsl.DslF
import io.parapet.core.Event.Start
import io.parapet.core.{Process, ProcessRef}
import io.parapet.nbody.api.Nbody
import io.parapet.nbody.api.Nbody.{Cmd, CmdType, Update}
import io.parapet.nbody.core.Body

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

class NBodySystemProcess(config: Config) extends Process[IO] {

  import dsl._

  private val bodies = Array.fill[Body](config.nbodySize) {
    new Body
  }

  val DT = 0.01
  val coordinatorId = "coordinator"
  private val logger = Logger[NBodySystemProcess]
  override val ref: ProcessRef = Constants.NBodySystemRef

  override def handle: Receive = {
    case Start =>
      eval(logger.info(config.toString)) ++
        (loadBodies ++ advance ++ sendUpdate)
          .handleError(err => eval(logger.error("start has failed", err)))
    case Req(sender, data) =>
      logger.debug(s"received req from $sender")
      val cmd = Cmd.parseFrom(data)
      cmd.getCmdType match {
        case CmdType.NEXT_ROUND => (advance ++ sendUpdate)
          .handleError(err => eval(logger.error("failed to process round", err)))
        case CmdType.UPDATE =>
          eval {
            val update = Update.parseFrom(cmd.getData)
            logger.debug(s"received update for ${update.getBodyCount} bodies")
            for (i <- 0 until update.getBodyCount) {
              val body = update.getBody(i)
              bodies(i).x = body.getX
              bodies(i).y = body.getY
              bodies(i).z = body.getZ

              bodies(i).vx = body.getVx
              bodies(i).vy = body.getVy
              bodies(i).vz = body.getVz

              bodies(i).mass = body.getMass

              // logger.debug(s"body $i has been updated")
              // printBody(j, bodies(j))
            }
          }
      }
  }

  def loadBodies: DslF[IO, Unit] = eval {
    val lines = Files.readAllLines(Paths.get(config.dataFile), StandardCharsets.UTF_8).asScala
    for (i <- lines.indices) {
      val parts = lines(i).split(",")
      val body = bodies(i)
      body.x = parts(0).toDouble
      body.y = parts(1).toDouble
      body.z = parts(2).toDouble

      body.vx = parts(3).toDouble * Body.DAYS_PER_YEAR
      body.vy = parts(4).toDouble * Body.DAYS_PER_YEAR
      body.vz = parts(5).toDouble * Body.DAYS_PER_YEAR
      body.mass = parts(6).toDouble

      if (i > 0) {
        body.mass = body.mass * Body.SOLAR_MASS
      }

    }
    var px = 0.0d
    var py = 0.0d
    var pz = 0.0d
    for (i <- lines.indices) {
      px += bodies(i).vx * bodies(i).mass
      py += bodies(i).vy * bodies(i).mass
      pz += bodies(i).vz * bodies(i).mass
    }
    bodies(0).offsetMomentum(px, py, pz);

    logger.info(s"${bodies.length} bodies loaded")
  }

  def advance: DslF[IO, Unit] = eval {
    for (i <- config.from until config.to) {
      for (j <- 0 until config.nbodySize) {
        if (i != j) {
          val dx = bodies(i).x - bodies(j).x
          val dy = bodies(i).y - bodies(j).y
          val dz = bodies(i).z - bodies(j).z

          val distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
          val mag = DT / (distance * distance * distance);
          bodies(i).vx -= dx * bodies(j).mass * mag
          bodies(i).vy -= dy * bodies(j).mass * mag
          bodies(i).vz -= dz * bodies(j).mass * mag
        }
      }
    }

    for (i <- config.from until config.to) {
      bodies(i).x += DT * bodies(i).vx
      bodies(i).y += DT * bodies(i).vy
      bodies(i).z += DT * bodies(i).vz
    }

  }

  def sendUpdate: DslF[IO, Unit] = eval {
    val updateBuilder = Nbody.Update.newBuilder()

    for (j <- config.from until config.to) {
      val body = bodies(j)
      updateBuilder.addBody(Nbody.Body.newBuilder()
        .setX(body.x)
        .setY(body.y)
        .setZ(body.z)
        .setVx(body.vx)
        .setVy(body.vy)
        .setVz(body.vz)
        .setMass(body.mass).build())
    }
    val update = updateBuilder
      .setFrom(config.from)
      .setTo(config.to).build()
    require(update.getBodyCount == (config.to - config.from))
    Cmd.newBuilder().setCmdType(CmdType.UPDATE).setData(update.toByteString).build()
  }.flatMap(cmd => Req(coordinatorId, cmd.toByteArray) ~> Constants.NodeRef)


  def printBody(i: Int, body: Body): Unit = {
    logger.debug(s"i=$i, x=${body.x}, y=${body.y}, z=${body.z}, vx=${body.vx}, vy=${body.vy}, vz=${body.vz}, mass=${body.mass}")
  }
}

object NBodySystemProcess {}