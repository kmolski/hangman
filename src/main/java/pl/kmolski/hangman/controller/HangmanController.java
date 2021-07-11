package pl.kmolski.hangman.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pl.kmolski.hangman.dao.HangmanGameRepository;
import pl.kmolski.hangman.model.HangmanDictionary;
import pl.kmolski.hangman.model.HangmanGame;
import pl.kmolski.hangman.model.InvalidGuessException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HangmanController {
    /**
     * Names of the cookies used in the "/stats" view.
     */
    private static final Set<String> COOKIE_NAMES = Set.of("winCount", "loseCount", "correctGuesses", "wrongGuesses");

    private HangmanGameRepository gameRepository;

    @Autowired
    private void setGameRepository(HangmanGameRepository gameRepository) {
        this.gameRepository = gameRepository;
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

    public HangmanGame createAndSaveGameModel() {
        var model = new HangmanGame();
        model.addWords(HangmanDictionary.DEFAULT_WORDS);
        model.nextRound();
        gameRepository.save(model);
        return model;
    }

    @RequestMapping(path="/addWords", method=RequestMethod.POST)
    public String addWords(@RequestParam("wordFile") MultipartFile wordFile, HttpSession session) throws IOException {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");
        if (gameModel == null) {
            return "redirect:/home";
        }

        try (var reader = new BufferedReader(new InputStreamReader(wordFile.getInputStream()))) {
            gameModel.addWords(reader.lines().collect(Collectors.toList()));
        }
        gameRepository.update(gameModel);

        return "redirect:/home";
    }

    @RequestMapping(path="/home")
    public String home(Model model, HttpSession session) {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");
        if (gameModel == null) {
            session.setAttribute("gameModel", createAndSaveGameModel());
            return "redirect:/add_words.html";
        }

        model.addAttribute("gameModel", gameModel);
        return "home";
    }

    @RequestMapping(path="/loadSave")
    public String loadSave(@RequestParam("id") Long id, HttpSession session) {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");
        if (gameModel != null) {
            gameRepository.update(gameModel);
        }

        var newModel = gameRepository.get(id);
        if (newModel.isEmpty()) {
            throw new RuntimeException("game save ID " + id + " does not exist!");
        }
        session.setAttribute("gameModel", newModel.get());
        return "redirect:/home";
    }

    @RequestMapping(path="/saves")
    public String saves(Model model) {
        model.addAttribute("saves", gameRepository.getAll());
        return "saves";
    }

    @RequestMapping(path="/skipWord")
    public String skipWord(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");
        if (gameModel == null) {
            return "redirect:/home";
        }

        gameModel.nextRound();
        if (gameModel.isGameOver()) {
            gameRepository.delete(gameModel);
            session.removeAttribute("gameModel");

            incrementCookieValue(request, response, "loseCount");
            return "redirect:/game_lost.html";
        } else {
            gameRepository.update(gameModel);
            return "redirect:/home";
        }
    }

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

    @RequestMapping(path="/gameOver")
    public String gameOver(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        var gameModel = (HangmanGame) session.getAttribute("gameModel");
        if (gameModel == null) {
            return "redirect:/home";
        }

        gameRepository.delete(gameModel);
        session.removeAttribute("gameModel");

        if (gameModel.didWin()) {
            incrementCookieValue(request, response, "winCount");
            return "redirect:/game_won.html";
        } else {
            incrementCookieValue(request, response, "loseCount");
            return "redirect:/game_lost.html";
        }
    }

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
            boolean isGuessCorrect = gameModel.tryLetter(guess);
            gameRepository.update(gameModel);

            if (gameModel.isGameOver()) {
                return "redirect:/gameOver";
            } else if (gameModel.isRoundOver()) {
                gameModel.nextRound();
                return "redirect:/round_over.html";
            }

            if (isGuessCorrect) {
                incrementCookieValue(request, response, "correctGuesses");
                return "redirect:/guess_correct.html";
            } else {
                incrementCookieValue(request, response, "wrongGuesses");
                return "redirect:/guess_wrong.html";
            }
        } catch (InvalidGuessException e) {
            //response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid guess: " + e.getMessage());
            return "redirect:/home";
        }
    }
}
