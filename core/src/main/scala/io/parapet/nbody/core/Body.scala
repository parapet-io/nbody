package io.parapet.nbody.core


/**
 * links:
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/program/nbody-java-1.html
 * https://gereshes.com/2018/05/07/what-is-the-n-body-problem/
 */
class Body {
  // position
  var x = 0.0d
  var y = 0.0d
  var z = 0.0d
  // velocity change over each tick in time
  var vx = 0.0d
  var vy = 0.0d
  var vz = 0.0d
  var mass = 0.0d

  def advance(dt: Double, other: Body): Unit = {
    val dx = x - other.x
    val dy = y - other.y
    val dz = z - other.z

    val distance = Math.sqrt(dx * dx + dy * dy + dz * dz) // https://mathworld.wolfram.com/L2-Norm.html
    val mag = dt / (distance * distance * distance)
    vx -= dx * other.mass * mag
    vy -= dy * other.mass * mag
    vz -= dz * other.mass * mag

    x += dt * vx
    y += dt * vy
    z += dt * vz
  }
}

object Body {
  val PI: Double = 3.141592653589793
  val SOLAR_MASS: Double = 4 * PI * PI
  val DAYS_PER_YEAR: Double = 365.24

}

