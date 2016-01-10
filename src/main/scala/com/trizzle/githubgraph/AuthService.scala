package com.trizzle.githubgraph


import reactivemongo.api.{Collection, MongoConnection, MongoDriver}
import reactivemongo.bson._
import reactivemongo.api.commands.WriteResult
import scala.async.Async._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
/**
  * Created by marktranter on 07/01/2016.
  */



class AuthService(implicit ctx: ExecutionContext){

  private val mongoConnectionString = System.getenv("GITHUBGRAPH_MONGODB_CONNECTION_STRING")

  private val driver = new MongoDriver

  private val connection: MongoConnection =
    MongoConnection.parseURI(mongoConnectionString).map { parsedUri =>
      driver.connection(parsedUri)
    }.get

  implicit val loginUserReader = Macros.reader[LoginUser]
  implicit val loginUserWriter = Macros.writer[LoginUser]
  implicit val checkStatesReader = Macros.reader[CheckStates]
  implicit val checkStatesWriter = Macros.writer[CheckStates]

  def saveUser(name: String, githubId: String, githubLogin: String, url: String, html_url: String, access_token: String): Future[String] =  async {

    val db = await(connection.database("githubgraph"))

    val collection = await(db.coll("logins"))

    val query = BSONDocument("githubId" -> githubId)

    val exists = await(collection.find(query).one[BSONDocument])


    exists match  {
      case Some(doc) =>  {
        val idOpt = doc.getAs[BSONObjectID]("_id")
        idOpt match {
          case Some(id) => {
            await(collection.update(query, BSONDocument(
              "$set" -> BSONDocument(
                "access_token" -> access_token))))
            id.stringify
          }
          case None => null
        }
      }
      case None => {
        val docId = BSONObjectID.generate
        val user = LoginUser(docId, name, githubId, githubLogin, url, html_url, access_token)
        val insertResult = await(collection.insert(user)).ok
        docId.stringify
      }
    }
  }

  def generateStateHash(): Future[String] = async {

    val uuid = java.util.UUID.randomUUID.toString
    val db = await(connection.database("githubgraph"))

    // Gets a reference to the collection "acoll"
    // By default, you get a BSONCollection.
    val collection = await(db.coll("checkstates"))
    await(collection.insert(CheckStates(uuid)))
    uuid
  }

  def findStateHash(hash: String): Future[Boolean] = async{
    val db = await(connection.database("githubgraph"))

    val collection = await(db.coll("checkstates"))

    val query = BSONDocument("uuid" -> hash)

    val exists = await(collection.find(query).one[BSONDocument])

    exists match  {
      case Some(doc) => {
        collection.remove(query)
        true
      }
      case None => false
    }
  }


}

case class CheckStates(uuid: String)

case class LoginUser(_id: BSONObjectID, name: String, githubId: String, githubLogin: String, url: String, html_url: String, access_token: String)