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

/**
 * Service class for the web app. Handles database operations and main game logic.
 *
 * @author Krzysztof Molski
 * @version 1.0
 */
@Service
public class HangmanGameService {

    /**
     * Repository of game state objects.
     */
    private HangmanGameRepository gameRepository;

    @Autowired
    private void setGameRepository(HangmanGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Create the game state object and persist it in the database.
     * The new game state contains the default dictionary words.
     * @return The game state object
     */
    public HangmanGame createAndSaveGameModel() {
        var model = new HangmanGame(new HangmanDictionary());
        model.addWords(HangmanDictionary.DEFAULT_WORDS);
        model.nextRound();
        gameRepository.save(model);
        return model;
    }

    /**
     * Load a game state from the database. The previous game state should be provided
     * as an argument to this method to ensure that no progress is lost.
     * @param prevModel The previous game state object
     * @param id The ID of the game state object that will be loaded
     * @return The requested game state object
     */
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

    /**
     * Get all game state object from the database
     * @return A list of game state objects
     */
    public List<HangmanGame> getAllGameSaves() {
        return gameRepository.getAll();
    }

    /**
     * Add words from the file to the game state object
     * @param wordFile The word file
     * @param gameModel The game state object
     * @throws IOException This operation may fail if the word file can not be opened
     */
    public void addWords(MultipartFile wordFile, HangmanGame gameModel) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(wordFile.getInputStream()))) {
            gameModel.addWords(reader.lines().collect(Collectors.toList()));
        }
        gameRepository.update(gameModel);
    }

    /**
     * Skip the current word in the game. If this causes the player to lose
     * the game, delete the game state object from the database.
     * @param gameModel The game state object
     */
    public void skipWord(HangmanGame gameModel) {
        gameModel.nextRound();
        if (gameModel.isGameOver()) {
            gameRepository.delete(gameModel);
        } else {
            gameRepository.update(gameModel);
        }
    }

    /**
     * Try the given letter. If the guess causes the player to lose the game,
     * the game state object will be removed from the DB and the HTTP session.
     * @param session The HTTP session that contains the game state
     * @param gameModel The game state object
     * @param guess The guessed letter
     * @return true if the guess is correct
     * @throws InvalidGuessException May be thrown if the guess is not a single letter
     */
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
