package models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * Created by Biacco42 on 2016/04/19.
  */
case class MailToken(
  id: UUID,
  userId: UUID,
  expirationDate: DateTime,
  tokenKind: String)

object MailToken {

  /**
    * Create MailToken instance easily.
    * @param user The user.
    * @param tokenKind The token kind which corresponds "confirm" or "reset"
    * @return New mail token instance.
    */
  def create(user: User, tokenKind: String): MailToken = MailToken(UUID.randomUUID(), user.userID, new DateTime().plusDays(1), tokenKind)

  /**
    * Converts the [MailToken] object to Json and vice versa.
    */
  implicit val jsonFormat = Json.format[MailToken]

}
