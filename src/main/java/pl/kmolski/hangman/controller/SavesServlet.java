package pl.kmolski.hangman.controller;

import org.thymeleaf.context.WebContext;
import pl.kmolski.hangman.HangmanApplication;
import pl.kmolski.hangman.dao.HangmanGameDAO;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet implementation for the Saves page.
 *
 * This servlet is responsible for displaying information about the
 * game saves that exist in the database. It is located under "/Saves".
 *
 * @author Krzysztof Molski
 * @version 1.0.0
 */
@WebServlet(name = "Saves", urlPatterns = {"/Saves"})
public class SavesServlet extends HttpServlet {
    /**
     * Injected data-access object for HangmanGame object management.
     */
    @EJB
    private HangmanGameDAO gameDAO;

    /**
     * Display information about the game saves that are in the database: the last word that was being
     * guessed, the number of words that were guessed correctly/are remaining and the miss count.
     * @param request The HTTP request.
     * @param response The response (an HTML page).
     * @throws IOException May be thrown if sending the redirect or creating the PrintWriter fails.
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        var ctx = new WebContext(request, response, getServletContext());
        ctx.setVariable("saves", gameDAO.getAll());
        HangmanApplication.getTemplateEngine().process("Saves", ctx, response.getWriter());
    }

    /**
     * Process GET requests from the client.
     * @param request The HTTP request.
     * @param response The response associated with the request.
     * @throws IOException May be thrown if sending the redirect fails.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }
}
