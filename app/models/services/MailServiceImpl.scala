package models.services

import java.util.UUID
import javax.inject.Inject

import controllers.routes
import models.daos.MailTokenDAO
import models.{MailToken, User}
import org.joda.time.DateTime
import views.html.mails
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.RequestHeader
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Biacco42 on 2016/04/18.
  */
class MailServiceImpl @Inject() (
  mailerClient: MailerClient,
  val messagesApi: MessagesApi,
  mailTokenDAO: MailTokenDAO) extends MailService with I18nSupport {

  def sendConfirm(user: User)(implicit request: RequestHeader): Future[String] = {
    val mailToken = MailToken.create(user, "confirm")
    mailTokenDAO.create(mailToken)
    val link = routes.SignUpController.mailConfirm(mailToken.id.toString).absoluteURL()
    Future(mailerClient.send(confirmMail(user, link)))
  }

  def confirmMail(user: User, link: String): Email = {
    Email(subject = Messages("mail.confirm.title"),
      from = Messages("mail.from"),
      to = Seq(user.email.getOrElse(throw new Exception("User.email is None."))),
      bodyText = Some(mails.welcomeTxt(user.firstName.getOrElse("User.firstname is None."), link).toString),
      bodyHtml = Some(mails.welcome(user.firstName.getOrElse("User.firstname is None."), link).toString))
  }

  def consumeToken(tokenId: UUID, kind: String): Future[Option[UUID]] = {
    mailTokenDAO.read(tokenId).map{
      case Some(MailToken(dbTokenId, userId, expirationDate, tokenKind)) =>
          mailTokenDAO.delete(dbTokenId)
          tokenValidation(userId, expirationDate, tokenKind == kind)
      case _ => None
    }
  }

  def saveToken(token: MailToken): Future[WriteResult] = mailTokenDAO.create(token)

  def tokenValidation(userId: UUID, expirationDate: DateTime, kindMatch: Boolean): Option[UUID] = {
    if (expirationDate.isAfterNow && kindMatch) Option(userId) else None
  }

}
