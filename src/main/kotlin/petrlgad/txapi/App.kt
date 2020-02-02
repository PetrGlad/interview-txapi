/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package petrlgad.txapi

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HelloHandler @JvmOverloads constructor(val greeting: String = "Hello, world!",
                                             val body: String? = null)
    : AbstractHandler() {

    @Throws(IOException::class, ServletException::class)
    override fun handle(target: String,
                        baseRequest: Request,
                        request: HttpServletRequest,
                        response: HttpServletResponse) {
        response.contentType = "text/html; charset=utf-8"
        response.status = HttpServletResponse.SC_OK
        val out = response.writer
        out.println("<h1>$greeting</h1>")
        if (body != null) {
            out.println(body)
        }
        baseRequest.isHandled = true
    }
}

fun main(args: Array<String>) {
    val server = Server(8080);
    server.handler = HelloHandler(body ="BLAH!!!")
    server.start();
    server.join();
}