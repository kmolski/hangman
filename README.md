# hangman

This is the further development of [my 5th semester project](https://github.com/kmolski/hangman-javaEE), a simple
hangman game that I had created during the Java in the Internet and Mobile devices class.

The backend has been migrated from bare Java Servlets and the embedded H2 database to Spring Web MVC and MariaDB.
The application server has changed from Payara/Glassfish 5 to Tomcat.
The frontend was built using HTML5 and Bootstrap CSS.

Built with:
-----------

- Java 11
- Spring Web MVC
- Hibernate ORM & Validation
- Thymeleaf HTML templates
- Bootstrap CSS
- MariaDB
- JUnit unit tests
- Tomcat application server (embedded)

Build & Run:
-----------

To build and run on the embedded Tomcat server:
```sh
mvn package cargo:run
```

The application will be available on `http://localhost:8080`

License:
--------

[MIT License](https://opensource.org/licenses/MIT)
