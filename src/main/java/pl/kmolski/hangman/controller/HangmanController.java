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
public class HangmanController {
    /**
     * Names of the cookies used in the "/stats" view.
     */
    private static final Set<String> COOKIE_NAMES = Set.of("winCount", "loseCount", "correctGuesses", "wrongGuesses");

    @RequestMapping(path="/addWords", method=RequestMethod.POST)
    public View addWords(@RequestParam(value="wordFile") MultipartFile wordFile, Model model) throws IOException {
        var gameModel = (HangmanGame) model.getAttribute("gameModel");
        if (gameModel == null) {
            return new RedirectView("home");
        }

        try (var reader = new BufferedReader(new InputStreamReader(wordFile.getInputStream()))) {
            gameModel.addWords(reader.lines().map(String::trim).map(String::toLowerCase)
                                     .collect(Collectors.toList()));
        }

        return new RedirectView("home");
    }

    @RequestMapping(path="/home")
    public String home() {
        return "home";
    }

    @RequestMapping(path="/stats")
    public View stats(HttpServletRequest request, Model model) {
        var gameModel = (HangmanGame) model.getAttribute("model");
        if (gameModel == null) {
            return new RedirectView("home");
        }

        model.addAllAttributes(Arrays.stream(request.getCookies())
             .filter(cookie -> COOKIE_NAMES.contains(cookie.getName()))
             .collect(Collectors.toMap(Cookie::getName, Cookie::getValue)));
        return new ThymeleafView("stats");
    }
}
