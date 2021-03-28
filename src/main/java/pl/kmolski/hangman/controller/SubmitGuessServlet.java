package pl.kmolski.hangman.controller;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import pl.kmolski.hangman.dao.HangmanGameDAO;
import pl.kmolski.hangman.model.HangmanGame;
import pl.kmolski.hangman.model.InvalidGuessException;

import java.io.IOException;

/**
 * Servlet implementation for the SubmitGuess page.
 *
 * This servlet is responsible for receiving and processing guesses sent by the user
 * and updating the model accordingly. It is located under "/SubmitGuess".
 *
 * @author Krzysztof Molski
 * @version 1.0.1
 */
@WebServlet(name = "SubmitGuess", urlPatterns = "/SubmitGuess")
public class SubmitGuessServlet extends HttpServlet {
    /**
     * Injected data-access object for HangmanGame object management.
     */
    @EJB
    private HangmanGameDAO gameDAO;

    /**
     * Find the appropriate cookie, and increment its numeric value by 1. If the cookie
     * does not exist, a new cookie with the provided name and value "1" is created.
     * @param request The request that contains the cookie.
     * @param response The response where the cookie will be sent back.
     * @param cookieName The name of the cookie.
     */
    private void incrementCookieValue(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(cookieName)) {
                int count = Integer.parseInt(cookie.getValue());
                cookie.setValue(Integer.toString(count + 1));
                cookie.setMaxAge(60 * 60 * 24 * 365);
                response.addCookie(cookie);
                return;
            }
        }
        var newCookie = new Cookie(cookieName, "1");
        newCookie.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(newCookie);
    }

    /**
     * Process the guess submission that was received from the client. If there's no model instance
     * in the current session, the client is redirected to HomeServlet. If the guess is invalid or
     * non-existent, an HTTP 400 "Bad Request" response is sent back to the client.
     * @param request The request that contains the guess.
     * @param response The response associated with the request.
     * @throws IOException May be thrown if sending the redirect fails.
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        HangmanGame model = (HangmanGame) session.getAttribute("model");
        if (model == null) {
            response.sendRedirect("Home");
            return;
        }

        String guess = request.getParameter("guess");
        if (guess == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "`guess` parameter not found.");
            return;
        }

        try {
            boolean isGuessCorrect = model.tryLetter(guess);

            if (model.isGameOver()) {
                gameDAO.delete(model);
                session.removeAttribute("model");

                incrementCookieValue(request, response, model.didWin() ? "winCount" : "loseCount");
                response.sendRedirect(model.didWin() ? "game_won.html" : "game_lost.html");
            } else if (model.isRoundOver()) {
                model.nextRound();
                gameDAO.update(model);

                response.sendRedirect("round_over.html");
            } else {
                gameDAO.update(model);

                incrementCookieValue(request, response, isGuessCorrect ? "correctGuesses" : "wrongGuesses");
                response.sendRedirect(isGuessCorrect ? "guess_correct.html" : "guess_wrong.html");
            }
        } catch (InvalidGuessException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid guess: " + e.getMessage());
        }
    }

    /**
     * Process POST requests from the client.
     * @param request The request that contains the guess.
     * @param response The response associated with the request.
     * @throws IOException May be thrown if sending the redirect fails.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    /**
     * Process GET requests from the client.
     * @param request The request that contains the guess.
     * @param response The response associated with the request.
     * @throws IOException May be thrown if sending the redirect fails.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }
}
