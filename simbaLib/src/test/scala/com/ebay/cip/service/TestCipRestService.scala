package com.ebay.cip.service

import com.ebay.cip.service.actor.PipelineStart
import org.squbs.unicomplex.RouteDefinition
import spray.http.MediaTypes._
import spray.httpx.encoding.Gzip
import spray.routing.Directives._


// this class defines our service behavior independently from the service actor
class TestCipRestService extends RouteDefinition {

  def route =
    get{
      path("hello") {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>squbsapi</i> on <i>squbs</i>, <i>spray-routing</i> and <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      } ~
      path(""".*\.html""".r) { name =>
          encodeResponse(Gzip) {
            getFromResource("html/" + name)
          }
        }
    } ~
    post{
      ctx => context.actorSelection("/user/framework/HttpParentActor/orchestratorRouter") ! PipelineStart(ctx)
    }
}