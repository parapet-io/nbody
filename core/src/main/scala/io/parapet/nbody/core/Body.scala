package io.parapet.nbody.core


/**
 * links:
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/program/nbody-java-1.html
 * https://gereshes.com/2018/05/07/what-is-the-n-body-problem/
 *
 * vec a;
 * for (i in 1..N)
 * {
 * vec r;
 * r.x = nbodies[i].x - body.x;
 * r.y = nbodies[i].y - body.y;
 * r.z = nbodies[i].z - body.z;
 *
 * float r2 = r.x r.x + r.y r.y + r.z * r.z + eps;
 * float r6 = r2 r2 r2;
 * float rI = 1.0f / sqrt(r6);
 *
 * float s = body.mas * rI;
 *
 * a.x += r.x * s;
 * a.y += r.y * s;
 * a.z += r.z * s;
 * }
 *
 * float half_dt = dt / 2.0f;
 * body.vx += half_dt * a.x;
 * body.vy += half_dt * a.y;
 * body.vz += half_dt * a.z;
 *
 * body.x += body.vx * dt;
 * body.y += body.vy * dt;
 * body.z += body.vz * dt;
 *
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

  def update(dt: Double, a: Vec): Unit = {
    val halfDt = dt / 2.0d
    vx += halfDt * a.x
    vy += halfDt * a.y
    vz += halfDt * a.z

    x += vx * dt
    y += vy * dt
    z += vz * dt
  }
}

object Body {
  val PI: Double = 3.141592653589793
  val SOLAR_MASS: Double = 4 * PI * PI
  val DAYS_PER_YEAR: Double = 365.24

}

