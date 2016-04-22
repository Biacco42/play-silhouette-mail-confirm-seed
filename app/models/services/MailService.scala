package models.services

import java.util.UUID

import models.User
import play.api.mvc.RequestHeader

import scala.concurrent.Future

/**
  * Created by Biacco42 on 2016/04/19.
  */
trait MailService {

  /**
    * Send address confirmation mail to [User].
    * @param user The user who the mail send to.
    */
  def sendConfirm(user: User)(implicit request: RequestHeader): Future[String]

  /**
    * Find the mail token and remove it from repo.
    * If this method find the token, returns Future(Some(userId)), otherwise returns Future(None).
    * @param tokenId The token id.
    * @param kind The token kind which corresponds to "confirm" or "reset".
    * @return The UUID of the User which corresponds to mail token.
    */
  def consumeToken(tokenId: UUID, kind: String): Future[Option[UUID]]

}
