import akka.actor.ActorSystem
import com.trizzle.githubgraph.{AuthServlet, GitHubGraphServlet}
import org.eclipse.jetty.server.{Handler, Server}
import org.eclipse.jetty.server.handler.{DefaultHandler, HandlerList, ResourceHandler}
import org.eclipse.jetty.servlet.{ServletHolder, DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher {
  private val system = ActorSystem()
  def main(args: Array[String]) {
    val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080

    val server = new Server(port)
    val context = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase("src/main/webapp")

    context.setEventListeners(Array(new ScalatraListener))


    val resource_handler = new ResourceHandler();
    resource_handler.setDirectoriesListed(true);
    //resource_handler.setWelcomeFiles(new String[]{ "index.html" });

    resource_handler.setResourceBase(".");

    val handlers = new HandlerList()
    handlers.setHandlers(Array[Handler]( context, resource_handler, new DefaultHandler()))
    server.setHandler(handlers)

    server.start
    server.join
  }
}