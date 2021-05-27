package io.parapet.nbody.node

import cats.effect.IO
import io.parapet.cluster.node.Req
import io.parapet.core.Dsl.DslF
import io.parapet.core.Event.Start
import io.parapet.core.{Process, ProcessRef}
import io.parapet.nbody.api.Nbody
import io.parapet.nbody.api.Nbody.{Cmd, CmdType}
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


  override val ref: ProcessRef = Constants.NBodySystemRef

  override def handle: Receive = {
    case Start => loadBodies
    case Req(_, data) =>
      val cmd = Cmd.parseFrom(data)
      cmd.getCmdType match {
        case CmdType.NEXT_ROUND =>
          advance ++ sendUpdate
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
  }

  def advance: DslF[IO, Unit] = eval {
    for (i <- config.from until config.to) {
      val body = bodies(i)
      for (j <- 0 until config.nbodySize) {
        if (i != j) {
          body.advance(DT, bodies(j))
        }
      }
    }
  }

  def sendUpdate(): DslF[IO, Unit] = eval {
    val size = config.to - config.from
    val arr = new Array[Nbody.Body](size)

    var i = 0
    for (j <- config.to until config.from) {
      val body = bodies(j)
      arr(i) = Nbody.Body.newBuilder()
        .setX(body.x)
        .setY(body.y)
        .setZ(body.z)
        .setVx(body.vx)
        .setVy(body.vy)
        .setVz(body.vz)
        .setMass(body.mass).build()
      i = i + 1
    }
    val update = Nbody.Update.newBuilder()
      .addAllBody(arr.toList.asJava)
      .setFrom(config.from)
      .setTo(config.to).build()
    update
  }.flatMap(update => Req(coordinatorId, update.toByteArray) ~> Constants.NodeRef)

}

object NBodySystemProcess {}