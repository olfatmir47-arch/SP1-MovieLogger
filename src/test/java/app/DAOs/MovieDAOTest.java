package app.DAOs;

import app.entities.*;
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
                "MovieLoggerPU",
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
    void shouldReturnAllMovies() {
        Movie movie1 = new Movie();
        movie1.setTitle("Harry Potter 1");
        movie1.setTmdbId(1);
        Movie movie2 = new Movie();
        movie2.setTitle("Harry Potter 2");
        movie2.setTmdbId(2);

        movieDAO.saveMovie(movie1);
        movieDAO.saveMovie(movie2);

        List<Movie> result = movieDAO.getAll();

        assertEquals(2, result.size());

        List<String> movies = result.stream().map(Movie::getTitle).toList();

        assertTrue(movies.contains("Harry Potter 1"));
        assertTrue(movies.contains("Harry Potter 2"));
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
    void shouldSaveMultipleMovies() {
        Movie movie1 = new Movie();
        movie1.setTitle("Grum");
        movie1.setTmdbId(1);
        Movie movie2 = new Movie();
        movie2.setTitle("Grummere");
        movie2.setTmdbId(2);

        List<Movie> saveMovies = List.of(movie1, movie2);

        movieDAO.saveMovies(saveMovies);

        List<Movie> result = movieDAO.getAll();

        assertEquals(2, result.size());

        List<String> movies = result.stream().map(Movie::getTitle).toList();

        assertTrue(movies.contains("Grum"));
        assertTrue(movies.contains("Grummere"));

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

    @Test
    void shouldReturnAllActorsFromMovie() {
        Cast actor1 = new Cast();
        actor1.setName("Cat");
        Cast actor2 = new Cast();
        actor2.setName("Dog");

        movieDAO.saveActor(actor1);
        movieDAO.saveActor(actor2);

        Movie movie = new Movie();
        movie.setTitle("Pets Movie");

        movie.setCast(new HashSet<>(List.of(actor1, actor2)));

        movieDAO.saveMovie(movie);

        List<Cast> result = movieDAO.getAllActorsByMovieTitle(movie);

        assertEquals(2, result.size());

        List<String> actors = result.stream().map(Cast::getName).toList();

        assertTrue(actors.contains("Cat"));
        assertTrue(actors.contains("Dog"));
    }

    @Test
    void ShouldReturnAllDirectorsByMovie() {
        Director director1 = new Director();
        director1.setName("Jack Lee");
        Director director2 = new Director();
        director2.setName("Lee Jack");

        movieDAO.saveDirector(director1);
        movieDAO.saveDirector(director2);

        Movie movie = new Movie();
        movie.setTitle("Jack Movie");

        movie.setDirectors(new HashSet<>(List.of(director1, director2)));

        movieDAO.saveMovie(movie);

        List<Director> result = movieDAO.getAllDirectorsByMovieTitle(movie);

        assertEquals(2, result.size());

        List<String> directors = result.stream().map(Director::getName).toList();

        assertTrue(directors.contains("Jack Lee"));
        assertTrue(directors.contains("Lee Jack"));
    }

    @Test
    void shouldFindDirectorByName() {
        Director director = new Director();
        director.setName("Cat Dog");

        movieDAO.saveDirector(director);

        Director result = movieDAO.getDirectorByName("Cat Dog");

        assertEquals("Cat Dog", result.getName());
    }

    @Test
    void shouldFindActorByName() {
        Cast actor = new Cast();
        actor.setName("Son Goku");

        movieDAO.saveActor(actor);

        Cast result = movieDAO.getCastByName("Son Goku");

        assertEquals("Son Goku", result.getName());
    }

    @Test
    void shouldSaveProductionCountry() {
        ProductionCountry country = new ProductionCountry();
        country.setName("Denmark");

        ProductionCountry savedCountry = movieDAO.saveProductionCountry(country);

        assertNotNull(savedCountry);
        assertTrue(savedCountry.getId() > 0);
        assertEquals("Denmark", savedCountry.getName());

        List<Movie> movies = movieDAO.getByProductionCountry("Japan");

        assertNotNull(movies);
    }

    @Test
    void shouldSaveDirector() {
        Director director = new Director();
        director.setName("Sam Samsung");

        Director savedDirector = movieDAO.saveDirector(director);

        assertNotNull(savedDirector);
        assertTrue(savedDirector.getId() > 0);
        assertEquals("Sam Samsung", savedDirector.getName());

        Director result = movieDAO.getDirectorByName("Sam Samsung");

        assertNotNull(result);
        assertEquals("Sam Samsung", result.getName());

    }

    @Test
    void shouldSaveActor() {
        Cast actor = new Cast();
        actor.setName("Conor Rain");

        Cast savedActor = movieDAO.saveActor(actor);

        assertNotNull(savedActor);
        assertTrue(savedActor.getId() > 0);
        assertEquals("Conor Rain", savedActor.getName());

        Cast result = movieDAO.getCastByName("Conor Rain");

        assertNotNull(result);
        assertEquals("Conor Rain", result.getName());
    }

    @Test
    void shouldFindMovieByTmdbId(){
        Movie movie = new Movie();
        movie.setTitle("Rain Down");
        movie.setTmdbId(1);

        movieDAO.saveMovie(movie);

        Movie result = movieDAO.getMovieByTmdbId(1);

        assertNotNull(result);
        assertEquals("Rain Down", result.getTitle());
        assertEquals(1, result.getTmdbId());
    }

}


