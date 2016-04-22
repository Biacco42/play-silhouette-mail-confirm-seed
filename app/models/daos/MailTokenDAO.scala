package models.daos

import java.util.UUID

import models.MailToken
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

/**
  * Created by Biacco42 on 2016/04/19.
  */
trait MailTokenDAO {

  def create(token: MailToken): Future[WriteResult]

  def read(tokenId: UUID): Future[Option[MailToken]]

  def delete(tokenId: UUID): Future[WriteResult]

}
