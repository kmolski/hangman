package pl.kmolski.hangman.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class HangmanController {
    @RequestMapping(path="/addWords")
    public RedirectView addWords() {
        return new RedirectView("Home");
    }

    @RequestMapping(path="/home")
    public String home() {
        return "home";
    }
}
