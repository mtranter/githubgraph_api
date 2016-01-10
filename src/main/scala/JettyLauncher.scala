import akka.actor.ActorSystem
import com.trizzle.githubgraph.{AuthServlet, GitHubGraphServlet}
import org.eclipse.jetty.server.Server
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

    context.addServlet(new ServletHolder(new GitHubGraphServlet(system)), "/*")
    context.addServlet(new ServletHolder(new AuthServlet(system)), "/*")

    server.setHandler(context)

    server.start
    server.join
  }
}