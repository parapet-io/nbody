package io.parapet.nbody.core

import io.parapet.nbody.core.Body.SOLAR_MASS


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

  def offsetMomentum(px: Double, py: Double, pz: Double): Body = {
    vx = -px / SOLAR_MASS
    vy = -py / SOLAR_MASS
    vz = -pz / SOLAR_MASS
    this
  }

}

object Body {
  val PI: Double = 3.141592653589793
  val SOLAR_MASS: Double = 4 * PI * PI
  val DAYS_PER_YEAR: Double = 365.24

}

