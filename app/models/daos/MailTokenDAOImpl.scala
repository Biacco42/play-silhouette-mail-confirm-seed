package models.daos
import java.util.UUID
import javax.inject.Inject

import models.MailToken
import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB
import reactivemongo.api.commands.WriteResult
import scala.concurrent.ExecutionContext.Implicits.global

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._

import scala.concurrent.Future

/**
  * Created by Biacco42 on 2016/04/19.
  */
class MailTokenDAOImpl @Inject() (db: DB) extends MailTokenDAO {

  def collection: JSONCollection = db.collection[JSONCollection]("mailToken")

  override def create(token: MailToken): Future[WriteResult] = collection.insert(token)

  override def delete(tokenId: UUID): Future[WriteResult] = collection.remove(Json.obj("id" -> tokenId))

  override def read(tokenId: UUID): Future[Option[MailToken]] = collection.find(Json.obj("id" -> tokenId)).one[MailToken]

}
