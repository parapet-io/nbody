package io.parapet.nbody.node

import cats.effect.IO
import com.typesafe.scalalogging.Logger
import io.parapet.cluster.node.Req
import io.parapet.core.Dsl.DslF
import io.parapet.core.Event.Start
import io.parapet.core.{Process, ProcessRef}
import io.parapet.nbody.api.Nbody
import io.parapet.nbody.api.Nbody.{Cmd, CmdType, Update}
import io.parapet.nbody.core.{Body, Vec}

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

              // logger.debug(s"body $i has been updated")
              // printBody(j, bodies(j))

              j = j + 1
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

      body.mass = parts(6).toDouble * Body.SOLAR_MASS
    }
    logger.info(s"${bodies.length} bodies loaded")
  }

  def advance: DslF[IO, Unit] = eval {
    for (i <- config.from until config.to) {
      val body = bodies(i)
      val a = new Vec
      for (j <- 0 until config.nbodySize) {
        if (i != j) {
          val r = new Vec
          r.x = bodies(j).x - body.x;
          r.y = bodies(j).y - body.y;
          r.z = bodies(j).z - body.z;

          val r2 = r.x * r.x + r.y * r.y + r.z * r.z + 1e-6
          val r6 = r2 * r2 * r2
          val rI = 1.0f / Math.sqrt(r6)

          val s = bodies(i).mass * rI

          a.x += r.x * s;
          a.y += r.y * s;
          a.z += r.z * s;
        }
      }
      body.update(DT, a)
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