package io.parapet.nbody.coordinator

import scopt.OParser

case class Config(
                   host: String = "",
                   port: Int = 0,
                   servers: String = "",
                   nbodySize: Int = 0,
                   nodeSize: Int = 0)

object Config {
  private val builder = OParser.builder[Config]
  private val parser = {
    import builder._
    OParser.sequence(
      programName("nboby coordinator"),
      head("nboby coordinator", "1.0"),

      opt[String]("servers")
        .action((x, c) => c.copy(servers = x))
        .required()
        .text("coma separated cluster servers addresses"),
      opt[String]("host")
        .action((x, c) => c.copy(host = x))
        .required()
        .text("host"),
      opt[Int]("port")
        .action((x, c) => c.copy(port = x))
        .required()
        .text("port"),
      opt[Int]("nbody-size")
        .action((x, c) => c.copy(nbodySize = x))
        .required()
        .text("number of body"),
      opt[Int]("node-size")
        .action((x, c) => c.copy(nodeSize = x))
        .required()
        .text("number of nbody nodes"),
    )
  }

  def apply(args: Array[String]): Config = {
    OParser.parse(parser, args, Config()) match {
      case Some(config) => config
      case _ => throw new IllegalArgumentException("bad program args")
    }
  }
}