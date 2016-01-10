package com.trizzle.githubgraph


import _root_.akka.actor.{ActorSystem, Actor}
import org.scalatra._
import scalate.ScalateSupport
import org.json4s.{FieldSerializer, JsonFormat, DefaultFormats, Formats}
import org.scalatra.json._
import scala.concurrent.ExecutionContext


class GitHubGraphServlet(system: ActorSystem) extends GitHubgraphStack with FutureSupport with JacksonJsonSupport with CorsSupport{

  protected implicit def executor: ExecutionContext = system.dispatcher

  protected implicit val jsonFormats: Formats = DefaultFormats

  private val gitHubClient = new GitHubGraphClient()

  before() {
    contentType = formats("json")
  }
  options("/*"){
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
  }
  get("/user/:username") {
    new AsyncResult() { val is =
      gitHubClient.getGraph(params("username"))
    }
  }

}
