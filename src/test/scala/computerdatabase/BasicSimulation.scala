package computerdatabase

import java.util.concurrent.ThreadLocalRandom

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class ComputerSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://computer-database.gatling.io")
//    .proxy(Proxy("localhost",8888).httpsPort(8888))
    .inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en,zh-CN;q=0.9,zh;q=0.8")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.135 Safari/537.36")

  var randomName = ThreadLocalRandom.current().nextInt(0,100)

  object SearchComputer{
    val feeder = csv("search.csv").random

    val searchComputer = exec(
      http("Home")
        .get("/")
    ).pause(1)
      .feed(feeder)
      .exec(
        http("Search")
          .get("/computers?f=${searchCriterion}")
//          .check(css("a:contains('${searchComputerName}')", "href").saveAs("computerURL"))
      )

  }

  object AddComputer{
    val addComputer = {
      exec(
        http("add")
          .post("/computers")
          .header("authority","computer-database.gatling.io")
          .header("content-type","application/x-www-form-urlencoded")
          .formParam("name","yingjiedeMac02")
          .formParam("introduced","2020-08-25")
          .formParam("discontinued","2020-10-31")
          .formParam("company","3")
          .check(status.is(200))
      )
    }
  }

  object EditComputer{
    val editComputer = tryMax(2){
      exec(
        http("view")
          .get("/computers/new")
      ).pause(1)
        .exec(
          http("edit")
            .post("/computers")
            .formParam("name", "yingjiedeMac02")
            .formParam("introduced", "2012-05-30")
            .formParam("discontinued", "")
            .formParam("company", "37")
            .check(status.is(session => 200 + ThreadLocalRandom.current.nextInt(2)))
        )
    }
  }
  val computer = scenario("user stream")
    .repeat(5){
      exec(SearchComputer.searchComputer)
    }
    .pause(10)
    .repeat(5){
      exec(AddComputer.addComputer)
    }
    .pause(10)
    .repeat(2){
      exec(EditComputer.editComputer)
    }
    .pause(2)


  setUp(
    computer.inject(
      rampUsersPerSec(5) to(10) during(5 minutes) randomized
    ).protocols(httpProtocol))


}
