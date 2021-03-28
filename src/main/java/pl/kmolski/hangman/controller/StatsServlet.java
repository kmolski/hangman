package pl.kmolski.hangman.controller;

import org.thymeleaf.context.WebContext;
import pl.kmolski.hangman.HangmanApplication;
import pl.kmolski.hangman.model.HangmanGame;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servlet implementation for the Stats page.
 *
 * This servlet is responsible for displaying information about the current state
 * and history of the games. It is located under "/Stats".
 *
 * @author Krzysztof Molski
 * @version 1.0.1
 */
@WebServlet(name = "Stats", urlPatterns = "/Stats")
public class StatsServlet extends HttpServlet {
    /**
     * Names of the cookies used in the HTML template.
     */
    private static final Set<String> COOKIE_NAMES = Set.of("winCount", "loseCount", "correctGuesses", "wrongGuesses");

    /**
     * Display information about the total number of wins/losses, correct/wrong guesses
     * number of words that were guessed correctly/are remaining and the miss count.
     * If there's no model instance in the current session, the client is redirected to HomeServlet.
     * @param request The HTTP request.
     * @param response The response (an HTML page).
     * @throws IOException May be thrown if sending the redirect or creating the PrintWriter fails.
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        HangmanGame model = (HangmanGame) session.getAttribute("model");

        if (model == null) {
            response.sendRedirect("Home");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        var ctx = new WebContext(request, response, getServletContext());
        ctx.setVariable("model", model);
        ctx.setVariables(Arrays.stream(request.getCookies())
                               .filter(cookie -> COOKIE_NAMES.contains(cookie.getName()))
                               .collect(Collectors.toMap(Cookie::getName, Cookie::getValue)));
        HangmanApplication.getTemplateEngine().process("Stats", ctx, response.getWriter());
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
