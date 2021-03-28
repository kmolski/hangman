package pl.kmolski.hangman.dao;

import pl.kmolski.hangman.model.HangmanGame;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * DAO class for HangmanGame objects.
 *
 * This class provides methods that save, delete, modify and fetch
 * HangmanGame objects from the application database.
 *
 * @author Krzysztof Molski
 * @version 1.0.1
 */
@Stateless
public class HangmanGameDAO {
    /**
     * The entity manager managed by the server persistence context.
     */
    @PersistenceContext(unitName = "hangman")
    private EntityManager em;

    /**
     * Persist the game in the database.
     * @param model The game model that will be saved.
     */
    public void save(HangmanGame model) {
        executeInsideTransaction(em -> em.persist(model));
    }

    /**
     * Update the save of the current game in the database.
     * @param model The game model that will be updated.
     */
    public void update(HangmanGame model) {
        executeInsideTransaction(em -> em.merge(model));
    }

    /**
     * Get the save of a game with the specified ID from the database.
     * @param id Game save ID.
     * @return The saved instance of the game with the specified ID.
     */
    public Optional<HangmanGame> get(Serializable id) {
        return Optional.ofNullable(em.find(HangmanGame.class, id));
    }

    /**
     * Get all game saves from the database.
     * @return A list of all game saves.
     */
    public List<HangmanGame> getAll() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<HangmanGame> criteria = builder.createQuery(HangmanGame.class);
        criteria.from(HangmanGame.class);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * Delete the save of the current game from the database.
     * @param model The game model that will be deleted.
     */
    public void delete(HangmanGame model) {
        executeInsideTransaction(em -> em.remove(em.contains(model) ? model : em.merge(model)));
    }

    /**
     * Execute an action inside an EntityTransaction.
     * @param action An action that is executed inside a transaction.
     */
    public void executeInsideTransaction(Consumer<EntityManager> action) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.accept(em);
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }
}
