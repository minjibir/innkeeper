package org.zalando.spearheads.innkeeper.oauth

import com.google.inject.{ Inject, Singleton }
import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import org.zalando.spearheads.innkeeper.oauth.OAuthJsonProtocol.authorizedUserFormat
import org.zalando.spearheads.innkeeper.utils.HttpClient

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
 * @author dpersa
 */
trait AuthService {
  def authenticate(token: String): Try[AuthenticatedUser]
}

@Singleton
class OAuthService @Inject() (val config: Config,
                              val httpClient: HttpClient,
                              implicit val executionContext: ExecutionContext) extends AuthService {

  val logger = LoggerFactory.getLogger(this.getClass)

  override def authenticate(token: String): Try[AuthenticatedUser] = {
    httpClient.callJson(url(token), Some(token)).map(_.convertTo[AuthenticatedUser])
  }

  private val OAUTH_URL = config.getString("oauth.url")

  private def url(token: String) = OAUTH_URL + token
}
