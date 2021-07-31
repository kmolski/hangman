package pl.kmolski.hangman.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pl.kmolski.hangman.model.HangmanGame;
import pl.kmolski.hangman.model.InvalidGuessException;
import pl.kmolski.hangman.service.HangmanGameService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main controller class for the web app. This controller's endpoints are located at the web server root.
 *
 * @author Krzysztof Molski
 * @version 1.0
 */
@Controller
public class HangmanGameController {
    /**
     * Names of the cookies used in the "/stats" view.
     */
    private static final Set<String> COOKIE_NAMES = Set.of("winCount", "loseCount", "correctGuesses", "wrongGuesses");

    /**
     * Game state management service.
     */
    private HangmanGameService gameService;

    @Autowired
    private void setGameService(HangmanGameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Find the appropriate cookie, and increment its numeric value by 1. If the cookie
     * does not exist, a new cookie with the provided name and value "1" is created.
     * @param request The request that contains the cookie.
     * @param response The response where the cookie will be sent back.
     * @param cookieName The name of the cookie.
     */
    private void incrementCookieValue(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        var cookie = Arrays.stream(request.getCookies())
                           .filter(c -> c.getName().equals(cookieName))
                           .findFirst().orElse(null);

        if (cookie != null) {
            int value = Integer.parseInt(cookie.getValue());
            cookie.setValue(Integer.toString(value + 1));
        } else {
            cookie = new Cookie(cookieName, "1");
        }

        cookie.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(cookie);
    }

    /**
     * Receive, process word files sent by the user, and updating the model accordingly.
     * If there's no model instance in the current session, the client is redirected to "/home".
     * @param wordFile The user-supplied word file
     */
    @RequestMapping(path="/addWords", method=RequestMethod.POST)
    public String addWords(@RequestParam("wordFile") MultipartFile wordFile, HttpSession session) throws IOException {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");

        if (gameModel != null) {
            gameService.addWords(wordFile, gameModel);
        }

        return "redirect:/home";
    }

    /**
     * Display the main screen of the game. Information about the current word
     * and the miss count is displayed along with the relevant controls. If there's
     * no model instance in the current session, a new instance is created.
     */
    @RequestMapping(path="/home")
    public String home(Model model, HttpSession session) {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");
        if (gameModel == null) {
            session.setAttribute("gameModel", gameService.createAndSaveGameModel());
            return "redirect:/add_words.html";
        }

        model.addAttribute("gameModel", gameModel);
        return "home";
    }

    /**
     * Process the save load request from the client. If a model instance does exist in the
     * current session, it will be saved to the database before any game save is loaded.
     * @param id Game save ID
     */
    @RequestMapping(path="/loadSave")
    public String loadSave(@RequestParam("id") Long id, HttpSession session) {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");

        session.setAttribute("gameModel", gameService.loadGameSave(gameModel, id));
        return "redirect:/home";
    }

    /**
     * Display information about the game saves that are in the database: the last word that was being
     * guessed, the number of words that were guessed correctly/are remaining and the miss count.
     */
    @RequestMapping(path="/saves")
    public String saves(Model model) {
        model.addAttribute("saves", gameService.getAllGameSaves());
        return "saves";
    }

    /**
     * Process the word skip request from the client. If there's no model instance
     * in the current session, the client is redirected to "/home".
     */
    @RequestMapping(path="/skipWord")
    public String skipWord(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");

        if (gameModel != null) {
            gameService.skipWord(gameModel);

            if (gameModel.isGameOver()) {
                session.removeAttribute("gameModel");
                incrementCookieValue(request, response, "loseCount");
                return "redirect:/game_lost.html";
            }
        }

        return "redirect:/home";
    }

    /**
     * Display information about the total number of wins/losses, correct/wrong guesses
     * number of words that were guessed correctly/are remaining and the miss count.
     * If there's no model instance in the current session, the client is redirected to "/home".
     */
    @RequestMapping(path="/stats")
    public String stats(HttpServletRequest request, HttpSession session, Model model) {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");
        if (gameModel == null) {
            return "redirect:/home";
        }

        model.addAttribute("gameModel", gameModel);
        model.addAllAttributes(Arrays.stream(request.getCookies())
             .filter(cookie -> COOKIE_NAMES.contains(cookie.getName()))
             .collect(Collectors.toMap(Cookie::getName, Cookie::getValue)));
        return "stats";
    }

    private String gameOverRoute(HangmanGame gameModel, HttpServletRequest request, HttpServletResponse response) {
        if (gameModel.didWin()) {
            incrementCookieValue(request, response, "winCount");
            return "redirect:/game_won.html";
        } else {
            incrementCookieValue(request, response, "loseCount");
            return "redirect:/game_lost.html";
        }
    }

    private String guessRoute(boolean isGuessCorrect, HttpServletRequest request, HttpServletResponse response) {
        if (isGuessCorrect) {
            incrementCookieValue(request, response, "correctGuesses");
            return "redirect:/guess_correct.html";
        } else {
            incrementCookieValue(request, response, "wrongGuesses");
            return "redirect:/guess_wrong.html";
        }
    }

    /**
     * Process the guess submission that was received from the client.
     * If there's no model instance in the current session, the client is redirected to "/home".
     * @param guess The user-supplied guess
     */
    @RequestMapping(path="/submitGuess")
    public String submitGuess(@RequestParam("guess") String guess,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session) {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");
        if (gameModel == null) {
            return "redirect:/home";
        }

        try {
            boolean isGuessCorrect = gameService.tryLetter(session, gameModel, guess);

            if (gameModel.isGameOver()) {
                return gameOverRoute(gameModel, request, response);
            } else if (gameModel.isRoundOver()) {
                return "redirect:/round_over.html";
            } else {
                return guessRoute(isGuessCorrect, request, response);
            }
        } catch (InvalidGuessException e) {
            return "redirect:/home";
        }
    }
}
