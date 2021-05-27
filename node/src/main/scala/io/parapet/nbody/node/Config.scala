package io.parapet.nbody.node

import scopt.OParser

case class Config(servers: String = "",
                  host: String = "",
                  port: Int = 0,
                  nodeId: Int = 0,
                  nodeSize: Int = 0,
                  nbodySize: Int = 0,
                  from: Int = 0,
                  to: Int = 0,
                  dataFile: String = "")

object Config {
  private val builder = OParser.builder[Config]
  private val parser = {
    import builder._
    OParser.sequence(
      programName("nboby"),
      head("nboby", "1.0"),

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
      opt[Int]("node-id")
        .action((x, c) => c.copy(nodeId = x))
        .required()
        .text("node id"),
      opt[Int]("nbody-size")
        .action((x, c) => c.copy(nbodySize = x))
        .required()
        .text("number of body"),
      opt[Int]("from")
        .action((x, c) => c.copy(from = x))
        .required()
        .text("start range"),
      opt[Int]("to")
        .action((x, c) => c.copy(to = x))
        .required()
        .text("end range"),
      opt[String]("data-file")
        .action((x, c) => c.copy(dataFile = x))
        .required()
        .text("file contains body data")
    )
  }

  def apply(args: Array[String]): Config = {
    OParser.parse(parser, args, Config()) match {
      case Some(config) => config
      case _ => throw new IllegalArgumentException("bad program args")
    }
  }
}
