import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

import ch.qos.logback.classic.{Level, LoggerContext}
import org.slf4j.LoggerFactory

class KeycloakLoginSimulator extends Simulation{

    // Number of login requests to simulate on execution
    val numberOfLogins = 20

    /***
        For debugging purposes, the following should be uncommented only when needed so as not to flood command line.

            - Declaring utility class for current simulation (required to log either of the following two options)
            val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

            - Logging all HTTP requests made during simulation
            context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))

            - Loggin all failed HTTP requests made during simulation
            context.getLogger("io.gatling.http").setLevel(Level.valueOf("DEBUG"))
    ***/
    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))
    /** Keycloak server should be running on your local machine for this URL to be available
            NOTE:   This is the most basic definition for a scenario's baseUrl. Details on what attributes
                    can be defined at this point can be found in the official 
                    gatling http_protocol docs at [https://gatling.io/docs/current/http/http_protocol/]
    **/
    val httpConfig = http.baseUrl("http://localhost:8080")
    .acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7,es;q=0.6")
		.doNotTrackHeader("1")
		.disableFollowRedirect
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36")

    
    /** Defining basic login test scenario for a test user "TESTACCOUNT" part of realm "LOAD-TESTING"
    This scenario is composed of two actions (each defined by .exec(...))

        ACTION 1 - First unauthenticated request:
            - Checks to see is http://localhost:8080/auth/realms/load-testing can be accessed. If it succeeds
                status 200 is sent and logged by gatling as a pass. Otherwise, exits the action and moves on to the
                next request.
                NOTE:   This is the URL for the test realm created on my local keycloak server. This would have to
                        be changed to work with running keycloak server we want to test.
        ACTION 2 - Authentication:
            - Checks if logging into a user that is a member of realm LOAD-TESTING is success with given username
                and password. 
    **/

    val headers_3 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
		"Origin" -> "http://localhost:8080",
		"Upgrade-Insecure-Requests" -> "1",
		"Accept-Encoding" -> "gzip, deflate, br",
		"Accept-Language" -> "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7,es;q=0.6",
		"Cache-Control" -> "max-age=0",
		"Connection" -> "keep-alive"
		)
    

    val scn = scenario("Testing the realm connection for test user")
        .exec(http("First unauthenticated request")
        .get("http://localhost:8080/auth/realms/load-testing/protocol/openid-connect/auth")
        .queryParam("redirect_uri", "http://localhost:8080/auth/realms/load-testing/")
        .queryParam("client_id", "account")
        .queryParam("response_type", "code")
        .queryParam("response_mode", "fragment")
        .queryParam("scope", "openid")
        .headers(headers_3)
        .check(status.is(200))
        .check(css("#kc-form-login")
                    .ofType[Node]
                    .transform(variable => {variable.getAttribute("action") })
                    .saveAs("loginurl"))
        )
        .exec(http("Login")
				.post("${loginurl}")
				.headers(headers_3)
				.formParam("username", "testaccount")
				.formParam("password", "Password")
				.check(status.is(302))
                .check(header("Location").saveAs("nextPage"))
		)
        .exec(http("Page after login")
                .get("${nextPage}")
                .queryParam("redirect_uri", "http://localhost:8080/auth/realms/load-testing/account/login-redirect")
                .headers(headers_3)
                .check(status.is(200))
        )
    /**
        The users variable is used to display on command prompt the progress of the current simulation. The 
        following is an example of a progress display on the command prompt during execution of this scenario is
        the number of logins was 10:

        ================================================================================
        2020-05-13 05:34:16                                          35s elapsed
        ---- Requests ------------------------------------------------------------------
        > Global                                                   (OK=16     KO=0     )
        > First unauthenticated request                            (OK=6      KO=0     )
        > Authentication                                           (OK=5      KO=0     )
        > Authentication Redirect 1                                (OK=5      KO=0     )

        ---- Users ---------------------------------------------------------------------
        [#############################---------------                              ] 40%
                  waiting: 4      / active: 2      / done: 4     
        ================================================================================
    **/

    val users = scenario("Users").exec(scn)

    setUp(users.inject(rampUsers(Integer.getInteger("users", numberOfLogins)) during (Integer.getInteger("ramp", 1) minutes))
    ).protocols(httpConfig)

}