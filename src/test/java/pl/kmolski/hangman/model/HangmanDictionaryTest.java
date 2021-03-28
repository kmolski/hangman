package pl.kmolski.hangman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the HangmanDictionary class.
 *
 * This class contains unit tests for the following operations:
 * adding new words, taking a random word, checking if the dictionary is empty.
 *
 * @author Krzysztof Molski
 * @version 1.0.3
 */
public class HangmanDictionaryTest {
    /**
     * The dictionary that is used during testing.
     */
    private HangmanDictionary dictionary;

    /**
     * Sets up a dictionary before each unit test.
     */
    @BeforeEach
    void setUp() {
        dictionary = new HangmanDictionary();
    }

    /**
     * Verify that words are correctly added to the dictionary.
     * @param words A list of words to add.
     */
    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {
            "boat,apple,orange",
            "green,blue,yellow",
            "horse,house,field",
            "ok,fine,correct"
    })
    void testAddWords(String words) {
        var additionalWords = new ArrayList<>(Arrays.asList(words.split(",")));
        dictionary.addWords(additionalWords);
        assertFalse(dictionary.isEmpty(), "dictionary is empty after addWords()");

        // Drain the dictionary.
        while (!dictionary.isEmpty()) {
            // Remove the word from `additionalWords` when it's taken from the dictionary.
            additionalWords.remove(dictionary.takeWord());
        }

        // `additionalWords` should be empty, because its words were added to `dictionary`
        // and then removed from `additionalWords` as they were encountered in `dictionary`.
        assertTrue(additionalWords.isEmpty(), "additionalWords is not empty!");
    }

    /**
     * Verify that an attempt to add a null list does not throw.
     * @param words A list of words to add.
     */
    @ParameterizedTest
    @NullSource
    void testAddNullWords(List<String> words) {
        // Drain the dictionary.
        while (!dictionary.isEmpty()) {
            dictionary.takeWord();
        }

        assertDoesNotThrow(() -> {
            // Passing null to `addWords()` must not throw
            dictionary.addWords(words);
        }, "Adding a null list has caused an exception!");

        assertTrue(dictionary.isEmpty(), "dictionary is not empty!");
    }

    /**
     * Verify that the dictionary gives out non-null words and correctly reports being empty.
     * @param words A list of words to add.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "boat,apple,orange",
            "green,blue,yellow",
            "horse,house,field",
            "ok,fine,correct"
    })
    void testDrainWords(String words) {
        var additionalWords = new ArrayList<>(Arrays.asList(words.split(",")));
        dictionary.addWords(additionalWords);

        // Take all words from the dictionary.
        while (!dictionary.isEmpty()) {
            // Words taken from a non-empty dictionary _must not_ be null.
            assertNotNull(dictionary.takeWord(), "Got null String from non-empty dictionary!");
        }

        // Words taken from an empty dictionary _must_ be null.
        assertNull(dictionary.takeWord(), "Got non-null String from empty dictionary!");
    }
}