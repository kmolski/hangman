package pl.kmolski.hangman.repo;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hangman.model.HangmanGame;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for HangmanGame objects.
 *
 * This class provides methods that save, delete, modify and fetch
 * HangmanGame objects from the application database.
 *
 * @author Krzysztof Molski
 * @version 1.0.1
 */
@Repository
@Transactional
public class HangmanGameRepository {
    /**
     * The entity manager managed by the server persistence context.
     */
    private SessionFactory sessionFactory;

    @Autowired
    private void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Persist the game in the database.
     * @param model The game model that will be saved.
     */
    public void save(HangmanGame model) {
        var session = sessionFactory.getCurrentSession();
        session.persist(model);
    }

    /**
     * Update the save of the current game in the database.
     * @param model The game model that will be updated.
     */
    public void update(HangmanGame model) {
        var session = sessionFactory.getCurrentSession();
        session.merge(model);
    }

    /**
     * Get the save of a game with the specified ID from the database.
     * @param id Game save ID.
     * @return The saved instance of the game with the specified ID.
     */
    public Optional<HangmanGame> get(Long id) {
        var session = sessionFactory.getCurrentSession();
        return Optional.ofNullable(session.get(HangmanGame.class, id));
    }

    /**
     * Get all game saves from the database.
     * @return A list of all game saves.
     */
    public List<HangmanGame> getAll() {
        var session = sessionFactory.getCurrentSession();
        return session.createQuery("from HangmanGame", HangmanGame.class).getResultList();
    }

    /**
     * Delete the save of the current game from the database.
     * @param model The game model that will be deleted.
     */
    public void delete(HangmanGame model) {
        var session = sessionFactory.getCurrentSession();
        session.remove(session.contains(model) ? model : session.merge(model));
    }
}
