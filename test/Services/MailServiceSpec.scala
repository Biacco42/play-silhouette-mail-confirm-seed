package services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.{MailToken, User}
import models.daos.MailTokenDAO
import models.services.MailServiceImpl
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.mailer.MailerClient
import play.api.mvc.RequestHeader
import play.api.test._
import play.api.test.Helpers._
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

/**
  * Created by Biacco42 on 2016/04/19.
  */
class MailServiceSpec extends PlaySpecification with Mockito with Inject {
  lazy val mailerClient = inject[MailerClient]
  lazy val messagesApi = inject[MessagesApi]

  "MailService#sendConfirm" should {
    "send an email for testUser" in new Context {
      val requestHeaderMock = mock[RequestHeader]
      requestHeaderMock.secure returns false
      requestHeaderMock.host returns "hoge.com"

      mailTokenDAOMock.create(any[MailToken]) returns Future(mock[WriteResult])

      val testMailService= new MailServiceImpl(mailerClient, messagesApi, mailTokenDAOMock)

      val ret = await(testMailService.sendConfirm(testUser)(requestHeaderMock))

      ret must beMatching("")
    }
  }

  "MailService#consumeToken" should {
    "removes token from repo and returns user Option[UUID]" in new ConsumeTokenContext {
      val testMailService = new MailServiceImpl(mailerClient, messagesApi, mailTokenDAOMock)

      val ret = await(testMailService.consumeToken(testToken, "confirm"))

      ret must beSome.which{_ == testUser.userID}
    }
    "fails when token doesn't match anyone and returns None" in new ConsumeTokenContext {
      val testInvalidToken = UUID.randomUUID()

      mailTokenDAOMock.read(any[UUID]) returns Future(None)

      val testMailService = new MailServiceImpl(mailerClient, messagesApi, mailTokenDAOMock)

      val ret = await(testMailService.consumeToken(testInvalidToken, "confirm"))

      ret must beNone
    }
    "fails when token kind doesn't match and returns None" in new ConsumeTokenContext {
      val testMailService = new MailServiceImpl(mailerClient, messagesApi, mailTokenDAOMock)

      val ret = await(testMailService.consumeToken(testToken, "reset"))

      ret must beNone
    }
    "fails when token has already expired and returns None" in new ConsumeTokenContext {
      mailTokenDAOMock.read(testToken) returns Future(Option(MailToken(testToken, testUser.userID, new DateTime().minusDays(1), "confirm")))

      val testMailService = new MailServiceImpl(mailerClient, messagesApi, mailTokenDAOMock)

      val ret = await(testMailService.consumeToken(testToken, "confirm"))

      ret must beNone
    }
  }

  trait Context extends Scope {
    val mailTokenDAOMock = mock[MailTokenDAO]

    val testUser = User(UUID.randomUUID(),
        LoginInfo("email", "hoge@piyo.com"),
        Option("Test"), Option("Taro"),
        Option("Test Taro"),
        Option("hoge@piyo.com"),
        None,
        None)
  }

  trait ConsumeTokenContext extends Context {
    val testToken = UUID.randomUUID()

    mailTokenDAOMock.read(testToken) returns Future(Option(MailToken(testToken, testUser.userID, new DateTime().plusDays(1), "confirm")))
    mailTokenDAOMock.delete(any[UUID]) returns Future(mock[WriteResult])
  }
}

trait Inject {
  lazy val injector = (new GuiceApplicationBuilder).injector()

  def inject[T : ClassTag]: T = injector.instanceOf[T]
}
