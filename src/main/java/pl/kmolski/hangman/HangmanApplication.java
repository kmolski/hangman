package pl.kmolski.hangman;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Application class for the web-based hangman.
 *
 * This class provides a way to initialize and access the
 * Thymeleaf template engine from other classes.
 *
 * @author Krzysztof Molski
 * @version 1.0.0
 */
public class HangmanApplication {
    /**
     * The entity manager for the `hangman` persistence unit (HangmanGame storage).
     */
    private static final EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("hangman");

    /**
     * The Thymeleaf template resolver for the application.
     */
    private static final TemplateEngine templateEngine;

    /**
     * This is an application class - it should not be instantiated.
     */
    private HangmanApplication() {}

    static {
        var templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    /**
     * Get the Thymeleaf TemplateEngine object.
     * @return The unique TemplateEngine object.
     */
    public static TemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}
