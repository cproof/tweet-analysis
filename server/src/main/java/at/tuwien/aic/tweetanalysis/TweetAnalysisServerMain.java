/*
 */

package at.tuwien.aic.tweetanalysis;

import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import static at.tuwien.aic.tweetanalysis.TweetAnalysis.shell;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import twitter4j.JSONException;
import twitter4j.JSONObject;

/**
 *
 * @author Thomas
 */
public class TweetAnalysisServerMain {
    
    public static void main(String[] args) throws InterruptedException, Exception {
        Server server = new Server(8080);
        server.setHandler(new TweetAnalysisServerHandler());
        server.start();
        server.join();
    }

    public static class TweetAnalysisServerHandler extends AbstractHandler {
        //Entry point
        @Override
        public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
            baseRequest.setHandled(true);
            response.setContentType("application/json;charset=utf-8");            
            
            switch(target) {
                case "/search":
                    handleSearchRequest(baseRequest, request, response);
                    break;
                case "/":
                default:
                    handleBadRequest(baseRequest, request, response);
            }
            
        }
        
        private void handleSearchRequest(Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException {
            try {
                JSONObject ret = new JSONObject();
                
                String query = baseRequest.getParameter("q"); //baseRequest.getAttribute("q").toString();
                ret.put("search", query);
                
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print(ret.toString());
            } catch (JSONException ex) {
                handleBadRequest(baseRequest, request, response);
            }
            
            
            
        }

        private void handleBadRequest(Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException {
             
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
            response.getWriter().println("{'Error' : 'No Request'}");
        }
        
    }
}
