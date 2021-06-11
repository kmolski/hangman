package pl.kmolski.hangman.model;

import com.sun.istack.NotNull;

import javax.persistence.*;
import java.io.Serializable;
import java.text.BreakIterator;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Model implementation for hangman.
 *
 * This class implements most of the game's logic - starting a new round,
 * guessing letters, managing the dictionary and win/lose conditions.
 *
 * @author Krzysztof Molski
 * @version 1.1.0
 */
@Entity
@Table(name = "game_saves")
public class HangmanGame implements Serializable {
    /**
     * The maximum number of incorrect guesses.
     */
    private static final int MAX_MISSES = 6;

    /**
     * The identifier of the HangmanGame instance in the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private Long id;

    /**
     * The dictionary from which words will be taken.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "id", referencedColumnName = "dict_id")
    private final HangmanDictionary dictionary;
    /**
     * The word that is currently being guessed.
     */
    @NotNull
    private String currentWord;
    /**
     * Letters that have been tried so far.
     */
    @NotNull
    private String guessedLetters;
    /**
     * Incorrect guess count.
     */
    @NotNull
    private int misses;
    /**
     * Number of words that were guessed correctly.
     */
    @NotNull
    private int wordsGuessed = 0;

    /**
     * The zero-argument constructor required for the JPA Entity annotation.
     */
    public HangmanGame() { dictionary = new HangmanDictionary(); }

    /**
     * Create a new model instance for the game, using the provided dictionary.
     * @param dictionary The dictionary that will be used in the game.
     */
    public HangmanGame(HangmanDictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Add new words to the dictionary. Duplicates are not removed.
     * @param words Collection of words to be added.
     */
    public void addWords(Collection<String> words) {
        dictionary.addWords(words);
    }

    /**
     * Start a new round of the game - select a new random word, reset the miss count and guessed letters.
     */
    public void nextRound() {
        currentWord = dictionary.takeWord();
        guessedLetters = " ";
        misses = 0;
    }

    /**
     * Get the identifier of this HangmanGame instance.
     * @return The HangmanGame identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Return the word that is being guessed right now, letters that
     * have not been tried so far are replaced with `_` characters.
     * @return The current word with secret characters masked out.
     */
    public String getMaskedWord() {
        String word;

        if (guessedLetters.length() == 0) {
            word = "_".repeat(currentWord.length());
        } else {
            word = currentWord.replaceAll("([^" + guessedLetters + "])", "_");
        }

        return word.chars().mapToObj(Character::toString)
                           .collect(Collectors.joining(" "));
    }

    /**
     * Get the incorrect guess count.
     * @return The number of incorrect guesses.
     */
    public int getMisses() {
        return misses;
    }

    /**
     * Check whether the current word has been guessed.
     * @return true if the current word has been guessed correctly.
     */
    public boolean isRoundOver() {
        return currentWord == null || currentWord.replaceAll("([" + guessedLetters + "])", "")
                                                 .isEmpty();
    }

    /**
     * Check if the game is over (either all words have been guessed, or the player has lost a round)
     * @return true if the game is over.
     */
    public boolean isGameOver() {
        return misses == MAX_MISSES || (isRoundOver() && dictionary.isEmpty()) || currentWord == null;
    }

    /**
     * Guess a letter and check if the guess was correct. The guess has to be a single letter.
     * @param guess Guessed character (has to be a single letter).
     * @return true if the guess was correct.
     * @throws InvalidGuessException Thrown if the guess is not a single letter.
     */
    public boolean tryLetter(String guess) throws InvalidGuessException {
        if (guess == null || guess.isEmpty()) {
            throw new InvalidGuessException("empty or null guess");
        }

        String lowercaseGuess = guess.toLowerCase();
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(lowercaseGuess);
        if (it.next() != it.last()) {
            throw new InvalidGuessException(lowercaseGuess);
        }

        guessedLetters += lowercaseGuess;
        boolean isGuessInWord = currentWord.contains(lowercaseGuess);
        if (!isGuessInWord) { ++misses; }

        if (isRoundOver()) { ++wordsGuessed; }
        return isGuessInWord;
    }

    /**
     * Get the word that is currently being guessed.
     * @return The current word.
     */
    public String getCurrentWord() {
        return currentWord;
    }

    /**
     * Get all guessed letters, separated by spaces.
     * @return The guessed letters in ascending order.
     */
    public String getGuessedLetters() {
        return guessedLetters.replaceAll("\\s", "")
                             .chars().distinct().sorted().mapToObj(Character::toString)
                             .collect(Collectors.joining(" "));
    }

    /**
     * Check if the player has won the game through guessing all words correctly.
     * @return true if the player has won the game.
     */
    public boolean didWin() {
        return dictionary.isEmpty() && dictionary.getWordCount() == wordsGuessed;
    }

    /**
     * Get the number of words that have been guessed correctly.
     * @return The correct guess count.
     */
    public int getWordsGuessed() {
        return wordsGuessed;
    }

    /**
     * Get the number of words left in the dictionary.
     * @return The remaining words count.
     */
    public int getWordsRemaining() {
        return dictionary.getWordCount() - wordsGuessed;
    }

    /**
     * equals() implementation for the HangmanGame class.
     * @param o The other object.
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HangmanGame game = (HangmanGame) o;
        return Objects.equals(id, game.id);
    }

    /**
     * hashCode() implementation for the HangmanGame class.
     * @return Hash code of the HangmanGame object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * toString() implementation for the HangmanGame class.
     * @return String representation the HangmanGame object.
     */
    @Override
    public String toString() {
        return "HangmanGame { id=" + id + " }";
    }
}
