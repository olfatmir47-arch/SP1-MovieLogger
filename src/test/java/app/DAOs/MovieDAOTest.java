package app.DAOs;

import app.entities.Genre;
import app.entities.Movie;
import app.entities.ProductionCountry;
import org.junit.jupiter.api.*;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MovieDAOTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("moviedb")
                    .withUsername("test")
                    .withPassword("test");


    private static EntityManagerFactory emf;
    private MovieDAO movieDAO;


    @BeforeAll
    static void setUpDatabase() {

        emf = Persistence.createEntityManagerFactory(
                "moviePU",
                java.util.Map.of(
                        "jakarta.persistence.jdbc.url",
                        postgres.getJdbcUrl(),
                        "jakarta.persistence.jdbc.user",
                        postgres.getUsername(),
                        "jakarta.persistence.jdbc.password",
                        postgres.getPassword()
                )
        );
    }


    @BeforeEach
    void setUp() {

        movieDAO = new MovieDAO(emf);
    }


    @AfterAll
    static void tearDownDatabase() {

        if (emf != null) {
            emf.close();
        }
    }


    @Test
    void shouldSaveMovie() {

        Movie movie = new Movie();

        movie.setTitle("Test Movie");
        movie.setReleaseDate(
                LocalDate.of(2024, 1, 1)
        );

        Movie savedMovie = movieDAO.saveMovie(movie);

        assertNotNull(savedMovie);
        assertTrue(savedMovie.getId() > 0);
        assertEquals("Test Movie", savedMovie.getTitle());
    }


    @Test
    void shouldFindMovieByTitle() {

        Movie movie = new Movie();

        movie.setTitle("The Danish Movie");
        movie.setReleaseDate(
                LocalDate.of(2023, 5, 10)
        );

        movieDAO.saveMovie(movie);

        List<Movie> result =
                movieDAO.getMovieByTitle("danish");

        assertEquals(1, result.size());
        assertEquals(
                "The Danish Movie",
                result.get(0).getTitle()
        );
    }


    @Test
    void titleSearchShouldBeCaseInsensitive() {

        Movie movie = new Movie();

        movie.setTitle("Interstellar");

        movieDAO.saveMovie(movie);

        List<Movie> result =
                movieDAO.getMovieByTitle("INTERSTELLAR");

        assertFalse(result.isEmpty());
        assertEquals(
                "Interstellar",
                result.get(0).getTitle()
        );
    }


    @Test
    void titleSearchShouldFindSubstring() {

        Movie movie = new Movie();

        movie.setTitle("The Lord of the Rings");

        movieDAO.saveMovie(movie);

        List<Movie> result =
                movieDAO.getMovieByTitle("lord");

        assertFalse(result.isEmpty());
        assertEquals(
                "The Lord of the Rings",
                result.get(0).getTitle()
        );
    }


    @Test
    void shouldFindMoviesByProductionCountry() {

        ProductionCountry country =
                new ProductionCountry();

        country.setName("Denmark");

        country =
                movieDAO.saveProductionCountry(country);


        Movie movie = new Movie();

        movie.setTitle("Danish Movie");
        movie.setProductionCountry(country);

        movieDAO.saveMovie(movie);


        List<Movie> result =
                movieDAO.getByProductionCountry("denmark");

        assertEquals(1, result.size());
        assertEquals(
                "Danish Movie",
                result.get(0).getTitle()
        );
    }


    @Test
    void shouldFindGenreByName() {

        Genre genre = new Genre();

        genre.setName("Comedy");

        movieDAO.saveGenre(genre);

        Genre result =
                movieDAO.getGenreByName("comedy");

        assertNotNull(result);
        assertEquals("Comedy", result.getName());
    }


    @Test
    void shouldSaveAndRetrieveMovieWithGenre() {

        Genre genre = new Genre();
        genre.setName("Drama");

        genre = movieDAO.saveGenre(genre);


        Movie movie = new Movie();

        movie.setTitle("Drama Movie");
        movie.setGenres(
                new HashSet<>(List.of(genre))
        );

        movieDAO.saveMovie(movie);


        List<Movie> result =
                movieDAO.getMovieByTitle("Drama Movie");

        assertEquals(1, result.size());

        Movie foundMovie = result.get(0);

        assertEquals(
                "Drama",
                foundMovie.getGenres()
                        .iterator()
                        .next()
                        .getName()
        );
    }


    @Test
    void shouldDeleteMovie() {

        Movie movie = new Movie();

        movie.setTitle("Movie To Delete");

        Movie savedMovie =
                movieDAO.saveMovie(movie);

        int id = savedMovie.getId();

        movieDAO.deleteMovie(id);

        List<Movie> result =
                movieDAO.getMovieByTitle("Movie To Delete");

        assertTrue(result.isEmpty());
    }
}