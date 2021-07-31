package pl.kmolski.hangman.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception that is thrown when the guess is not a single letter.
 *
 * @author Krzysztof Molski
 * @version 1.1
 */
@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="The guess is not a single letter")
public class InvalidGuessException extends Exception {
    /**
     * Create a new InvalidGuessException for the incorrect guess.
     * @param guess The guess taken from the user.
     */
    public InvalidGuessException(String guess) {
        super(guess);
    }
}
