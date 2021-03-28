package pl.kmolski.hangman.model;

import com.sun.istack.NotNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Dictionary implementation for hangman.
 *
 * This class is a container for words and supports the following operations:
 * adding new words, taking a random word, checking if the dictionary is empty.
 *
 * @author Krzysztof Molski
 * @version 1.0.6
 */
@Entity
@Table(name = "dictionary_saves")
public class HangmanDictionary {
    /**
     * The default set of words for the dictionary.
     */
    public static final List<String> DEFAULT_WORDS = List.of("koło", "drzwi", "drzewo", "powóz", "pole", "słońce");

    /**
     * The identifier of the HangmanDictionary in the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="dict_id")
    @NotNull
    private Long id;
    /**
     * An ArrayList that contains the dictionary's words.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @NotNull
    private final List<String> words = new ArrayList<>();
    /**
     * Random number generator that is used to generate array indices.
     */
    @Transient
    private final Random randomGenerator = new Random();
    /**
     * The number of words inside the dictionary.
     */
    @NotNull
    private int wordCount = 0;

    /**
     * Pick a random word (the selected word is removed from the dictionary).
     * @return A random word from the dictionary.
     */
    public String takeWord() {
        if (words.isEmpty()) {
            return null;
        } else {
            int randomIndex = randomGenerator.nextInt(words.size());
            return words.remove(randomIndex);
        }
    }

    /**
     * Add new words to the dictionary. Duplicates are not removed.
     * @param words A collection of words to be added.
     */
    public void addWords(Collection<String> words) {
        if (words == null) { return; }
        this.words.addAll(words);
        this.wordCount += words.size();
    }

    /**
     * Check whether the dictionary is empty or not.
     * @return true if the dictionary is empty.
     */
    public boolean isEmpty() {
        return words.isEmpty();
    }

    /**
     * Get the number of words inside the dictionary.
     * @return Number of words in the dictionary.
     */
    public int getWordCount() {
        return wordCount;
    }
}
