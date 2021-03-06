import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher.JsonMatchers
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class RESTSpec extends Specification with JsonMatchers {
  "REST endpoint" should {

    "return 400 for an unknown model" in new WithApplication {
      val result = route(FakeRequest(GET, "/api/hoge")).get

      status(result) must equalTo(400)
      charset(result) must beSome("utf-8")
      contentType(result) must beSome("application/json")
    }

    "return 404 for an unknown id" in new WithApplication {
      val result = route(FakeRequest(GET, "/api/beerbrand/1234567")).get

      status(result) must equalTo(404)
      charset(result) must beSome("utf-8")
      contentType(result) must beSome("application/json")
    }

    "return list of objects" in new WithApplication {
      val result = route(FakeRequest(GET, "/api/beerbrand/")).get

      status(result) must equalTo(OK)
      charset(result) must beSome("utf-8")
      contentType(result) must beSome("application/json")

      val json = Json.parse(contentAsString(result))
      json must beAnInstanceOf[JsArray]
      json.asInstanceOf[JsArray].value.size must equalTo(6)
    }

    "return filtered list of objects 1" in new WithApplication {
      val result = route(FakeRequest(GET, "/api/beerbrand?country_eq=Japan")).get

      status(result) must equalTo(OK)
      charset(result) must beSome("utf-8")
      contentType(result) must beSome("application/json")

      val json = Json.parse(contentAsString(result))
      json must beAnInstanceOf[JsArray]
      json.asInstanceOf[JsArray].value.size must equalTo(1)
      
      val str = contentAsString(result)
      str must /#(0) /("country" -> "Japan")
    }

    "return filtered list of objects 2" in new WithApplication {
      val result = route(FakeRequest(GET, "/api/beerbrand?strength_ge=5.0")).get

      status(result) must equalTo(OK)
      charset(result) must beSome("utf-8")
      contentType(result) must beSome("application/json")

      val json = Json.parse(contentAsString(result))
      json must beAnInstanceOf[JsArray]
      json.asInstanceOf[JsArray].value.size must equalTo(5)

      val str = contentAsString(result)
      str must /#(0) /("strength" -> (be_>=(5.0) ^^ ((_:String).toDouble)))
      str must /#(1) /("strength" -> (be_>=(5.0) ^^ ((_:String).toDouble)))
      str must /#(2) /("strength" -> (be_>=(5.0) ^^ ((_:String).toDouble)))
      str must /#(3) /("strength" -> (be_>=(5.0) ^^ ((_:String).toDouble)))
      str must /#(4) /("strength" -> (be_>=(5.0) ^^ ((_:String).toDouble)))
    }

    "return object with specified id" in new WithApplication {
      val result = route(FakeRequest(GET, "/api/beerbrand/1")).get

      status(result) must equalTo(OK)
      charset(result) must beSome("utf-8")
      contentType(result) must beSome("application/json")

      val json = contentAsString(result)
      json must / ("id" -> 1)
      json must / ("name" -> "Zillertal")
      json must / ("country" -> "Austria")
      json must / ("style" -> "白ビール")
      json must / ("tasty" -> true)
      json must / ("strength" -> 5.0)
      json must / ("createdAt" -> ".*".r)
      json must / ("updatedAt" -> ".*".r)
    }

    "create requested object" in new WithApplication {
      val result = route(
          FakeRequest(POST, "/api/beerbrand/")
          .withHeaders(("Content-Type", "application/json"))
          .withJsonBody(Json.parse("""{
            "name":"TestBeer",
            "country":"TestCountry",
            "style":"白ビール",
            "tasty":false,
            "strength":99.0,
            "comment":"Test comment.",
            "createdAt":0,
            "updatedAt":0
          }"""))
        ).get

      status(result) must equalTo(OK)
      charset(result) must beSome("utf-8")
      contentType(result) must beSome("application/json")
    }

    "update requested object" in new WithApplication {
      val result = route(
          FakeRequest(PUT, "/api/beerbrand/1")
          .withHeaders(("Content-Type", "application/json"))
          .withJsonBody(Json.parse("""{
            "name":"TestBeer",
            "country":"TestCountry",
            "style":"白ビール",
            "tasty":false,
            "strength":99.0,
            "comment":"Test comment.",
            "createdAt":0,
            "updatedAt":0
          }"""))
        ).get

      status(result) must equalTo(OK)
      charset(result) must beSome("utf-8")
      contentType(result) must beSome("application/json")
    }

    "delete requested object" in new WithApplication {
      val result = route(
          FakeRequest(DELETE, "/api/beerbrand/1")
        ).get

      status(result) must equalTo(OK)
      charset(result) must beSome("utf-8")
      contentType(result) must beSome("application/json")
    }

    "handle RPC request with one argument" in new WithApplication {
      val result = route(
        FakeRequest(POST, "/api/beerbrand/findStronger")
        .withHeaders(("Content-Type", "application/json"))
        .withJsonBody(Json.parse("""{
          "strength":5.5
        }"""))
      ).get

      val json = Json.parse(contentAsString(result))
      json must beAnInstanceOf[JsArray]
      json.asInstanceOf[JsArray].value.size must equalTo(1)

      val str = contentAsString(result)
      str must /#(0) /("strength" -> (be_>(5.0) ^^ ((_:String).toDouble)))
    }

    "handle RPC request with two arguments" in new WithApplication {
      val result = route(
        FakeRequest(POST, "/api/beerbrand/findByStyleAndCountry")
        .withHeaders(("Content-Type", "application/json"))
        .withJsonBody(Json.parse("""{
          "style":"ピルスナー",
          "country":"Japan"
        }"""))
      ).get

      val json = Json.parse(contentAsString(result))
      json must beAnInstanceOf[JsArray]
      json.asInstanceOf[JsArray].value.size must equalTo(1)

      val str = contentAsString(result)
      str must /#(0) / ("name" -> "Asahi Super Dry")
      str must /#(0) / ("style" -> "ピルスナー")
      str must /#(0) / ("country" -> "Japan")
    }
  }
}
