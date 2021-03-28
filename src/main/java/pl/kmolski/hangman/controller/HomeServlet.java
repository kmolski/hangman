package pl.kmolski.hangman.controller;

import org.thymeleaf.context.WebContext;
import pl.kmolski.hangman.HangmanApplication;
import pl.kmolski.hangman.dao.HangmanGameDAO;
import pl.kmolski.hangman.model.HangmanDictionary;
import pl.kmolski.hangman.model.HangmanGame;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet implementation for the Home page.
 *
 * This servlet is responsible for displaying information about the current state
 * (miss count, the current word, etc.) of the game. It is located under "/Home".
 *
 * @author Krzysztof Molski
 * @version 1.0.1
 */
@WebServlet(name = "Home", urlPatterns = {"/Home"})
public class HomeServlet extends HttpServlet {
    /**
     * Injected data-access object for HangmanGame object management.
     */
    @EJB
    private HangmanGameDAO gameDAO;

    /**
     * Display the main screen of the game. Information about the current word
     * and the miss count is displayed along with the relevant controls. If there's
     * no model instance in the current session, a new instance is created.
     * @param request The HTTP request.
     * @param response The response (an HTML page).
     * @throws IOException May be thrown if sending the redirect or creating the PrintWriter fails.
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        HangmanGame model = (HangmanGame) session.getAttribute("model");

        if (model == null || model.isGameOver()) {
            model = new HangmanGame();
            model.addWords(HangmanDictionary.DEFAULT_WORDS);
            model.nextRound();
            gameDAO.save(model);

            session.setAttribute("model", model);
            response.sendRedirect("add_words.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        var ctx = new WebContext(request, response, getServletContext());
        ctx.setVariable("model", model);
        HangmanApplication.getTemplateEngine().process("Home", ctx, response.getWriter());
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
