package pl.kmolski.hangman.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.kmolski.hangman.model.HangmanDictionary;
import pl.kmolski.hangman.model.HangmanGame;
import pl.kmolski.hangman.model.InvalidGuessException;
import pl.kmolski.hangman.repo.HangmanGameRepository;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HangmanGameService {

    private HangmanGameRepository gameRepository;

    @Autowired
    private void setGameRepository(HangmanGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public HangmanGame createAndSaveGameModel() {
        var model = new HangmanGame(new HangmanDictionary());
        model.addWords(HangmanDictionary.DEFAULT_WORDS);
        model.nextRound();
        gameRepository.save(model);
        return model;
    }

    @Transactional
    public HangmanGame loadGameSave(HangmanGame prevModel, Long id) {
        if (prevModel != null) {
            gameRepository.update(prevModel);
        }

        var newModel = gameRepository.get(id);
        if (newModel.isEmpty()) {
            throw new RuntimeException("game save ID " + id + " does not exist!");
        }
        return newModel.get();
    }

    public List<HangmanGame> getAllGameSaves() {
        return gameRepository.getAll();
    }

    public void addWords(MultipartFile wordFile, HangmanGame gameModel) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(wordFile.getInputStream()))) {
            gameModel.addWords(reader.lines().collect(Collectors.toList()));
        }
        gameRepository.update(gameModel);
    }

    public void skipWord(HangmanGame gameModel) {
        gameModel.nextRound();
        if (gameModel.isGameOver()) {
            gameRepository.delete(gameModel);
        } else {
            gameRepository.update(gameModel);
        }
    }

    @Transactional
    public boolean tryLetter(HttpSession session, HangmanGame gameModel, String guess) throws InvalidGuessException {
        boolean isGuessCorrect = gameModel.tryLetter(guess);
        gameRepository.update(gameModel);

        if (gameModel.isGameOver()) {
            gameRepository.delete(gameModel);
            session.removeAttribute("gameModel");
        } else if (gameModel.isRoundOver()) {
            gameModel.nextRound();
        }

        return isGuessCorrect;
    }
}
