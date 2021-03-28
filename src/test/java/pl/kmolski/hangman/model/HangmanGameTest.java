package pl.kmolski.hangman.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the HangmanGame class.
 *
 * This class contains unit tests for the game's logic - starting the next round,
 * guessing letters, managing the dictionary and win/lose conditions.
 *
 * @author Krzysztof Molski
 * @version 1.0.3
 */
public class HangmanGameTest {
    /**
     * The model that is used during testing.
     */
    private HangmanGame model;

    /**
     * Sets up a model and its dictionary before each unit test.
     */
    @BeforeEach
    void setUp() {
        HangmanDictionary dictionary = new HangmanDictionary();
        model = new HangmanGame(dictionary);
    }

    /**
     * Verify that the `nextRound()` method resets `misses` and `guessedLetters` correctly.
     * @param letter A letter that is given to `tryLetter()`.
     */
    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c", "e", "i", "h", "o", "n", "t"})
    void testReset(String letter) {
        model.addWords(HangmanDictionary.DEFAULT_WORDS);
        model.nextRound();
        String maskedWord = model.getMaskedWord();

        assertDoesNotThrow(() -> {
            if (model.tryLetter(letter)) {
                // We've guessed correctly, so the current `maskedWord` must be different.
                assertNotEquals(maskedWord, model.getMaskedWord(), "maskedWord did not change!");
            } else {
                // We have missed, which means that `misses` must be greater than 0.
                assertTrue(model.getMisses() > 0, "miss count did not change!");
            }

            model.nextRound();
            // After resetting the model, the incorrect guess count must be zero,
            // and the `maskedWord` must contain only `_` characters.
            assertEquals(model.getMisses(), 0, "incorrect guess count is not 0!");
            assertTrue(model.getMaskedWord().matches("[_ ]*"), "maskedWord contains chars other than '_'");
        }, "An exception has occurred:");
    }

    /**
     * Verify that the `getMaskedWord()` masks the current word correctly.
     * @param words A list of words to add.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "boat,apple,orange,green,blue,yellow",
            "horse,house,field,ok,fine,correct"
    })
    void testWordMasking(String words) {
        var additionalWords = new ArrayList<>(Arrays.asList(words.split(",")));
        model.addWords(additionalWords);

        while (!model.isGameOver()) {
            model.nextRound();

            // At this point, the masked word should consist of `_` characters only.
            assertTrue(model.getMaskedWord().matches("_*"), "maskedWord contains chars other than '_'");

            String currentWord = model.getCurrentWord();
            BreakIterator it = BreakIterator.getCharacterInstance();
            it.setText(currentWord);
            String firstLetter = currentWord.substring(0, it.next());

            assertDoesNotThrow(() -> {
                // Because the first letter of `currentWord` was used, the result of `tryLetter` should be true.
                assertTrue(model.tryLetter(firstLetter), "The current word does not contain its first letter!");
                // The masked word should now consist of the first letter of `currentWord` and `_` letters.
                assertTrue(model.getMaskedWord().matches("[" + firstLetter + "_]*"),
                        "maskedWord does not contain the first letter of the current word!");
            }, "An exception has occurred:");
        }
    }

    /**
     * Verify that the `isRoundOver()` condition is correct.
     * @param words A list of words to add.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "boat,apple,orange,green,blue,yellow",
            "horse,house,field,ok"
    })
    void testRoundOverCondition(String words) {
        var additionalWords = new ArrayList<>(Arrays.asList(words.split(",")));
        model.addWords(additionalWords);

        // The remaining word count must be greater than zero in the beginning.
        assertTrue(model.getWordsRemaining() > 0, "The remaining word count is 0!");

        while (!model.isGameOver()) {
            model.nextRound();

            String currentWord = model.getCurrentWord();
            BreakIterator it = BreakIterator.getCharacterInstance();
            it.setText(currentWord);

            assertDoesNotThrow(() -> {
                // Iterate over the letters of the current word.
                int start = it.first();
                for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
                    // `tryLetter` should always return true, as the letters are taken directly from `currentWord`.
                    assertTrue(model.tryLetter(currentWord.substring(start, end)),
                            "The current word does not contain its own letter!");
                }
            }, "An exception has occurred:");

            // After guessing all the letters correctly, we should have won the round.
            assertTrue(model.isRoundOver(), "The round is not over after correctly guessing all letters!");
        }
    }

    /**
     * Verify that the `isGameOver()` condition is correct.
     * @param words A list of words to add.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "boat,apple,orange",
            "green,blue,yellow",
            "horse,house,field",
            "ok,fine,correct"
    })
    void testGameOverCondition(String words) {
        var additionalWords = new ArrayList<>(Arrays.asList(words.split(",")));
        model.addWords(additionalWords);

        // The remaining word count must be greater than zero in the beginning.
        assertTrue(model.getWordsRemaining() > 0, "The remaining word count is 0!");

        // Skip all words in the dictionary.
        while (!model.isGameOver()) {
            model.nextRound();
        }

        // After skipping all the words, the game should be over and we should've lost.
        assertTrue(model.isGameOver(), "The game is not over after skipping all words!");
        assertFalse(model.didWin(), "The game should be lost after skipping all words!");
    }

    /**
     * Verify that trying a letter succeeds only if the current word contains it.
     * @param letter A letter that is passed to `tryLetter()`.
     */
    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c", "e", "i", "h", "o", "n", "t"})
    void testTryLetter(String letter) {
        model.addWords(HangmanDictionary.DEFAULT_WORDS);
        model.nextRound();

        assertDoesNotThrow(() -> {
            String currentWord = model.getCurrentWord();
            // If the current word contains the letter, `tryLetter()` must return true.
            assertEquals(currentWord.contains(letter), model.tryLetter(letter),
                    "The current words contains the letter, but the guess is incorrect (or vice versa)!");
        }, "An exception has occurred:");
    }

    /**
     * Verify that trying an empty or null letter fails.
     * @param letter An empty/null letter that is passed to `tryLetter()`.
     */
    @ParameterizedTest
    @NullAndEmptySource
    void testRejectNullOrEmptyGuess(String letter) {
        model.addWords(HangmanDictionary.DEFAULT_WORDS);
        model.nextRound();

        // Empty or null guesses must be rejected.
        assertThrows(InvalidGuessException.class, () -> model.tryLetter(letter),
                "An empty or null guess was not rejected!");
    }

    /**
     * Verify that trying a single letter guess succeeds and trying a longer guess fails.
     * @param letter  A single letter that is passed to `tryLetter()`.
     * @param tooLong A long guess (more than 1 letter) that is passed to `tryLetter()`.
     */
    @ParameterizedTest
    @CsvSource({
            "'l', 'long'",
            "'w', 'wrong'",
            "'e', 'excessive'",
            "'i', 'incorrect'"
    })
    void testRejectLongGuess(String letter, String tooLong) {
        model.addWords(HangmanDictionary.DEFAULT_WORDS);
        model.nextRound();

        // Guesses that are exactly 1 letter long are accepted.
        Assertions.assertDoesNotThrow(() -> model.tryLetter(letter), "A single letter was not accepted!");

        // Guesses longer than 1 letter are rejected.
        assertThrows(InvalidGuessException.class, () -> model.tryLetter(tooLong),
                "A multi-letter guess was not rejected!");
    }
}