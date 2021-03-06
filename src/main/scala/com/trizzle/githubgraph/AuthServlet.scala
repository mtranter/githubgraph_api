package com.trizzle.githubgraph

import akka.actor.ActorSystem
import dispatch.:/
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{CorsSupport, AsyncResult, UrlGeneratorSupport, FutureSupport}
import org.scalatra.json.JacksonJsonSupport
import scala.async.Async._
import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by marktranter on 06/01/2016.
  */
class AuthServlet(system: ActorSystem) extends GitHubgraphStack with FutureSupport with JacksonJsonSupport with UrlGeneratorSupport with CorsSupport{

  private val ClientSecret = System.getenv("GITHUBGRAPH_GITHUB_CLIENT_SECRET")
  private val ClientId = System.getenv("GITHUBGRAPH_GITHUB_CLIENT_ID")
  private val ApiHost = System.getenv("API_HOST")
  private val FrontEndHost = System.getenv("FRONT_END_HOST")

  private val AuthService = new AuthService()

  protected implicit val jsonFormats: Formats = DefaultFormats

  protected implicit def executor: ExecutionContext = system.dispatcher

  options("/*"){
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
  }

  get("/") {
    val redirectUrl = ApiHost + url(callback)
    val clientId = ClientId
    val addr = async {
      val state = await(AuthService.generateStateHash())
      s"https://github.com/login/oauth/authorize?client_id=$clientId&redirect_uri=$redirectUrl&state=$state"
    }

    new AsyncResult { val is =
      for { r <- addr } yield redirect(r)
    }
  }

  val callback = get("/oauthresponse") {
    val code = params.getOrElse[String]("code", halt(400))
    val state = params.getOrElse[String]("state",halt(400))
    val redirectUrl = ApiHost + url(complete, "code" -> code)

    val result = async {
      val stateOk = await(AuthService.findStateHash(state))
      if(!stateOk) {
        halt(403)
      }else {

        val req = (:/("github.com/login/oauth/access_token") <<?
          Map("client_id" -> ClientId,
            "client_secret" -> ClientSecret,
            "code" -> code,
            "state" -> state,
            "redirect_uri" -> redirectUrl)).POST

        val reqWithContentType = req.addHeader("Accept", "application/json").secure
        val response = await(JsonRestClient.request[OAuthResponse](reqWithContentType))
        val githubClient = new GitHubGraphClient(response.access_token)
        val user = await(githubClient.getCurrentUser())
        val id = await(AuthService.saveUser(user.name, user.id.toString(), user.login, user.url, user.html_url, response.access_token))

        FrontEndHost + s"?usrid=$id"

      }
    }

    new AsyncResult { val is =
      for { r <- result} yield redirect(r)
    }

  }

  val complete = get("/complete/:code") {
    val code = params.getOrElse[String]("code", halt(400))
    redirect(FrontEndHost + s"?cr=$code")
  }


}

case class OAuthResponse(token_type: String, scope: String, access_token: String)