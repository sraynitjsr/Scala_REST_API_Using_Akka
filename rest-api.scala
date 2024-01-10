import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object SimpleRestApi extends App {
  implicit val system: ActorSystem = ActorSystem("simple-rest-api")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val route =
    pathEnd {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, """{"message": "Hello, World!"}"""))
      }
    }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  bindingFuture.onComplete {
    case Success(binding) =>
      println(s"Server online at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}")
    case Failure(ex) =>
      println(s"Failed to bind to localhost:8080: ${ex.getMessage}")
      system.terminate()
  }

  // Graceful shutdown
  scala.sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
