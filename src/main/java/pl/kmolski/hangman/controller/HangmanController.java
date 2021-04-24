package pl.kmolski.hangman.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.spring5.view.ThymeleafView;
import pl.kmolski.hangman.model.HangmanGame;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@SessionAttributes("gameModel")
public class HangmanController {
    /**
     * Names of the cookies used in the "/stats" view.
     */
    private static final Set<String> COOKIE_NAMES = Set.of("winCount", "loseCount", "correctGuesses", "wrongGuesses");

    /**
     * Find the appropriate cookie, and increment its numeric value by 1. If the cookie
     * does not exist, a new cookie with the provided name and value "1" is created.
     * @param request The request that contains the cookie.
     * @param response The response where the cookie will be sent back.
     * @param cookieName The name of the cookie.
     */
    private void incrementCookieValue(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        var cookie = Arrays.stream(request.getCookies())
                           .filter(c -> c.getName().equals(cookieName)).findFirst().orElse(null);
        if (cookie != null) {
            cookie.setValue(Integer.toString(Integer.parseInt(cookie.getValue() + 1)));
        } else {
            cookie = new Cookie(cookieName, "1");
        }
        cookie.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(cookie);
    }

    @ModelAttribute("gameModel")
    public HangmanGame gameModel() {
        var model = new HangmanGame();
        model.addWords(HangmanDictionary.DEFAULT_WORDS);
        model.nextRound();
        return model;
    }

    @RequestMapping(path="/addWords", method=RequestMethod.POST)
    public String addWords(@RequestParam("wordFile") MultipartFile wordFile,
                           @ModelAttribute("gameModel") HangmanGame gameModel) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(wordFile.getInputStream()))) {
            gameModel.addWords(reader.lines().map(String::trim).map(String::toLowerCase)
                                     .collect(Collectors.toList()));
        }

        return "redirect:/home";
    }

    @RequestMapping(path="/home")
    public String home(@ModelAttribute("gameModel") HangmanGame gameModel) {
        return "home";
    }

    @RequestMapping(path="/stats")
    public String stats(HttpServletRequest request, Model model) {
        model.addAllAttributes(Arrays.stream(request.getCookies())
             .filter(cookie -> COOKIE_NAMES.contains(cookie.getName()))
             .collect(Collectors.toMap(Cookie::getName, Cookie::getValue)));
        return "stats";
    }

    @RequestMapping(path="/submitGuess")
    public String submitGuess(@RequestParam("guess") String guess,
                              @ModelAttribute("gameModel") HangmanGame gameModel,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              SessionStatus status) {
        try {
            boolean isGuessCorrect = gameModel.tryLetter(guess);

            if (gameModel.isGameOver()) {
                //gameDAO.delete(model);
                status.setComplete();
                incrementCookieValue(request, response, gameModel.didWin() ? "winCount" : "loseCount");
                return (gameModel.didWin() ? "redirect:/game_won.html" : "redirect:/game_lost.html");
            } else if (gameModel.isRoundOver()) {
                gameModel.nextRound();
                return "redirect:/round_over.html";
            } else {
                incrementCookieValue(request, response, isGuessCorrect ? "correctGuesses" : "wrongGuesses");
                return (isGuessCorrect ? "redirect:/guess_correct.html" : "redirect:/guess_wrong.html");
            }
        } catch (InvalidGuessException e) {
            //response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid guess: " + e.getMessage());
            return "redirect:/home";
        }
    }
}
