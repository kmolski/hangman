package pl.kmolski.hangman;

import pl.kmolski.hangman.model.InvalidGuessException;

import java.util.Collection;

/**
 * Interface for the model implementations for hangman.
 *
 * @author Krzysztof Molski
 * @version 1.0.2
 */
public interface HangmanGameModel {
    /**
     * Add new words to the dictionary. Duplicates are not removed.
     * @param words Collection of words to be added.
     */
    void addWords(Collection<String> words);

    /**
     * Start a new round of the game - select a new random word, reset the miss count and guessed letters.
     */
    void nextRound();

    /**
     * Guess a letter and check if the guess was correct. The guess has to be a single letter.
     * @param guess Guessed character (has to be a single letter).
     * @return true if the guess was correct.
     * @throws InvalidGuessException Thrown if the guess is not a single letter.
     */
    boolean tryLetter(String guess) throws InvalidGuessException;

    /**
     * Get the word that is currently being guessed.
     * @return The current word.
     */
    String getCurrentWord();

    /**
     * Return the word that is being guessed right now, letters that
     * have not been tried so far are replaced with filler characters.
     * @return The current word with secret characters masked out.
     */
    String getMaskedWord();

    /**
     * Get all letters that have been guessed.
     * @return The guessed letters.
     */
    String getGuessedLetters();

    /**
     * Get the incorrect guess count.
     * @return The number of incorrect guesses.
     */
    int getMisses();

    /**
     * Check if the game is over (either all words have been exhausted, or the player has lost a round)
     * @return true if the game is over.
     */
    boolean isGameOver();


    /**
     * Check if the player has won the game through guessing all words correctly.
     * @return true if the player has won the game.
     */
    boolean didWin();

    /**
     * Check whether the current word has been guessed.
     * @return true if the current word has been guessed correctly.
     */
    boolean isRoundOver();

    /**
     * Get the number of words that have been guessed correctly.
     * @return The correct guess count.
     */
    int getWordsGuessed();

    /**
     * Get the number of words left in the dictionary.
     * @return The remaining words count.
     */
    int getWordsRemaining();
}
