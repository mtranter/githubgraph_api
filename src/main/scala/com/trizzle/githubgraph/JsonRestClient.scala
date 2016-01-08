package com.trizzle.githubgraph

/**
  * Created by marktranter on 01/01/2016.
  */

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import dispatch.{url, Http, as, Req}
import org.json4s._
import java.lang.reflect.{Type, ParameterizedType}
import scala.Function.const
import scala.concurrent.{Promise, ExecutionContext, Future}
import scala.util._
import org.json4s.jackson.Json
import scala.async.Async._

object JsonRestClient {

  private val mapper = new ObjectMapper()
  implicit val formats = DefaultFormats

  mapper.registerModule(DefaultScalaModule)

  def request[T: Manifest](req: Req)(implicit ctx: ExecutionContext): Future[T] = async {
    val stringResult = await(Http(req OK as.String))
    deserialize[T](stringResult)
  }

  def get[T: Manifest](req: Req)(implicit ctx: ExecutionContext): Future[T] = async {
    val stringResult = await(Http(req OK as.String))
    deserialize[T](stringResult)
  }

  def get[T: Manifest](addr: String)(implicit ctx: ExecutionContext): Future[T] = {
    get(url(addr))
  }

  def deserialize[T: Manifest](value: String) : T =
    mapper.readValue(value, typeReference[T])

  private [this] def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])
  }

  private [this] def typeFromManifest(m: Manifest[_]): Type = {
    if (m.typeArguments.isEmpty) { m.erasure }
    else new ParameterizedType {
      def getRawType = m.erasure
      def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray
      def getOwnerType = null
    }
  }
}