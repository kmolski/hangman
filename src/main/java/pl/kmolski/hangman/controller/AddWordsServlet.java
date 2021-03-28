package pl.kmolski.hangman.controller;

import pl.kmolski.hangman.model.HangmanGame;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Servlet implementation for the AddWords page.
 *
 * This servlet is responsible for receiving and processing word files sent by the user
 * and updating the model accordingly. It is located under "/AddWords".
 *
 * @author Krzysztof Molski
 * @version 1.0.1
 */
@WebServlet(name = "AddWords", urlPatterns = {"/AddWords"})
@MultipartConfig(maxFileSize = 4096, maxRequestSize = 6144)
public class AddWordsServlet extends HttpServlet {
    /**
     * Process the file submission that was received from the client. If there's no
     * model instance in the current session, the client is redirected to HomeServlet.
     * If the request does not contain a multipart form that contains a `wordFile` part,
     * an HTTP 400 "Bad Request" response will be sent back to the client.
     * @param request The request that contains the word file.
     * @param response The response associated with the request.
     * @throws IOException May be thrown if sending the redirect or opening the word file fails.
     * @throws ServletException May be thrown if the form does not contain a `wordFile` part.
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        HangmanGame model = (HangmanGame) session.getAttribute("model");
        if (model == null) {
            response.sendRedirect("Home");
            return;
        }

        Part wordFilePart = request.getPart("wordFile");
        if (wordFilePart == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "`wordFile` request part not found!");
            return;
        }

        try (var reader = new BufferedReader(new InputStreamReader(wordFilePart.getInputStream()))) {
            model.addWords(reader.lines().map(String::trim).map(String::toLowerCase).collect(Collectors.toList()));
        }

        response.sendRedirect("Home");
    }

    /**
     * Process POST requests from the client.
     * @param request The HTTP request.
     * @param response The response associated with the request.
     * @throws IOException May be thrown if sending the redirect fails.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
