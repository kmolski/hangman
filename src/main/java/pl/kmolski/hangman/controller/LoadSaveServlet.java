package pl.kmolski.hangman.controller;

import pl.kmolski.hangman.dao.HangmanGameDAO;
import pl.kmolski.hangman.model.HangmanGame;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * Servlet implementation for the LoadSave page.
 *
 * This servlet is responsible for processing save load requests from the user, fetching the
 * game saves from the database and updating the session. It is located under "/LoadSave".
 *
 * @author Krzysztof Molski
 * @version 1.0.0
 */
@WebServlet(name = "LoadSave", urlPatterns = {"/LoadSave"})
public class LoadSaveServlet extends HttpServlet {
    /**
     * Injected data-access object for HangmanGame object management.
     */
    @EJB
    private HangmanGameDAO gameDAO;

    /**
     * Process the save load request from the client. If a model instance does exist in the
     * current session, it will be saved to the database before any game save is loaded.
     * If the game save ID is invalid or missing, an HTTP 400 "Bad Request" response is sent back.
     * @param request The request that contains the game save ID.
     * @param response The response associated with the request.
     * @throws IOException May be thrown if sending the redirect fails.
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        HangmanGame model = (HangmanGame) session.getAttribute("model");
        if (model != null) {
            gameDAO.update(model);
        }

        String idParamString = request.getParameter("id");
        if (idParamString == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "`id` parameter not found.");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParamString);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, idParamString + " is not a valid game save ID!");
            return;
        }

        Optional<HangmanGame> newModel = gameDAO.get(id);
        if (newModel.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "game save ID " + id + " does not exist!");
            return;
        }

        session.setAttribute("model", newModel.get());
        response.sendRedirect("Home");
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
