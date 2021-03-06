package org.zalando.spearheads.innkeeper.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.stream.ActorMaterializer
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import scala.collection.immutable.Seq
import scala.language.implicitConversions

/**
 * @author dpersa
 */
object AcceptanceSpecsHelper extends ScalaFutures with Matchers {

  private[routes] val baseUri = "http://localhost:9080"

  override implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  implicit val system = ActorSystem("main-actor-system")

  implicit val materializer = ActorMaterializer()

  def entityString(response: HttpResponse): String = {
    response.entity.dataBytes
      .map(bs => bs.utf8String)
      .runFold("")(_ + _)
      .futureValue
  }

  def getSlashHosts(token: String = ""): HttpResponse = doGet(s"$baseUri/hosts", token)

  private[routes] def doDelete(requestUri: String, token: String = ""): HttpResponse = {
    makeRequest(requestUri, token, HttpMethods.DELETE)
  }

  private[routes] def doGet(requestUri: String, token: String = ""): HttpResponse = {
    makeRequest(requestUri, token)
  }

  private[routes] def makeRequest(requestUri: String, token: String = "", method: HttpMethod = HttpMethods.GET): HttpResponse = {
    val futureResponse = Http().singleRequest(HttpRequest(
      uri = requestUri,
      method = method,
      headers = headersForToken(token)))
    futureResponse.futureValue
  }

  private[routes] def headersForToken(token: Option[String]): Seq[HttpHeader] = {
    val headers = token match {
      case Some(token) => Seq[HttpHeader](Authorization(OAuth2BearerToken(token)))
      case None        => Seq()
    }
    headers
  }

  implicit def stringToOption(string: String): Option[String] = {
    string match {
      case "" | null => None
      case str       => Option(str)
    }
  }
}

case class AcceptanceSpecToken(token: String, userName: String, realm: String, scope: String) {
  override val toString = s"$token-$userName-$realm-$scope"

  val teamName = userName.replace("user~", "team")
}

object AcceptanceSpecToken {
  implicit def AcceptanceSpecTokenToString(acceptanceSpecToken: AcceptanceSpecToken): String = acceptanceSpecToken.toString

  val READ_TOKEN = AcceptanceSpecToken("token", "user~1", "employees", "route.read")
  val WRITE_TOKEN = AcceptanceSpecToken("token", "user~1", "employees", "route.write")
  val ADMIN_TOKEN = AcceptanceSpecToken("token", "user~1", "employees", "route.admin")
  val ADMIN_TEAM_TOKEN = AcceptanceSpecToken("token", "user~3", "employees", "route.read")
  val INVALID_TOKEN = "invalid"
}
