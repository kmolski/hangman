package pl.kmolski.hangman.model;

/**
 * An exception that is thrown when the guess is not a single letter.
 *
 * @author Krzysztof Molski
 * @version 1.0.1
 */
public class InvalidGuessException extends Exception {
    /**
     * Create a new InvalidGuessException for the incorrect guess.
     * @param guess The guess taken from the user.
     */
    public InvalidGuessException(String guess) {
        super(guess);
    }
}
