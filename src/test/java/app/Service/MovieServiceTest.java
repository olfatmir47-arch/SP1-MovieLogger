package app.Service;

import app.DAOs.MovieDAO;
import app.DTOs.*;
import app.entities.*;
import app.service.MovieService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
public class MovieServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("moviedb")
                    .withUsername("test")
                    .withPassword("test");


    private static EntityManagerFactory emf;
    private MovieDAO movieDAO;
    private MovieService movieService;
    private MovieDTO movieDTO;
    private CreditsDTO creditsDTO;
    private CrewDTO directorCrew;
    private CastDTO actor1;
    private CastDTO actor2;

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
        movieService = new MovieService(emf);


        movieDTO = new MovieDTO();
        movieDTO.setId(999);
        movieDTO.setTitle("Base Movie");
        movieDTO.setReleaseDate(LocalDate.of(2026, 9, 20));

        directorCrew = new CrewDTO();
        directorCrew.setId(5001);
        directorCrew.setName("Director One");
        directorCrew.setJob("Director");

        actor1 = new CastDTO();
        actor1.setId(1001);
        actor1.setName("Actor One");
        actor1.setCharacter("Hero");

        actor2 = new CastDTO();
        actor2.setId(1002);
        actor2.setName("Actor Two");
        actor2.setCharacter("Villain");

        creditsDTO = new CreditsDTO();
        creditsDTO.setCrew(List.of(directorCrew));
        creditsDTO.setCast(List.of(actor1, actor2));
    }


    @AfterAll
    static void tearDownDatabase() {

        if (emf != null) {
            emf.close();
        }
    }

    @Test
    void shouldSaveMoviesWithCredits() {

        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setId(1);
        movieDTO.setTitle("movie1");
        movieDTO.setReleaseDate(LocalDate.of(2026, 9, 20));

        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setName("comedy");
        movieDTO.setGenres(List.of(genreDTO));

        ProductionCountryDTO countryDTO = new ProductionCountryDTO();
        countryDTO.setName("Denmark");
        movieDTO.setProductionCountries(List.of(countryDTO));

        CreditsDTO creditsDTO = new CreditsDTO();
        CastDTO actor1 = new CastDTO(1, "Cat Rain", "Superman");
        CastDTO actor2 = new CastDTO(2, "Cat Sun", "Batman");
        creditsDTO.setCast(List.of(actor1, actor2));

        CrewDTO directorCrew = new CrewDTO();
        directorCrew.setId(1);
        directorCrew.setName("Sam Tree");
        directorCrew.setJob("Director");
        creditsDTO.setCrew(List.of(directorCrew));

        MovieDTO saved = movieService.saveMovie(movieDTO, creditsDTO);

        assertNotNull(saved);
        assertEquals("movie1", saved.getTitle());
        assertEquals(LocalDate.of(2026, 9, 20), saved.getReleaseDate());

        Movie movie = movieDAO.getMovieByTmdbId(1);
        assertNotNull(movie);

        List<Cast> cast = movieDAO.getAllActorsByMovieTitle(movie);
        assertEquals(2, cast.size());

        List<Director> directors = movieDAO.getAllDirectorsByMovieTitle(movie);
        assertEquals(1, directors.size());
        assertEquals("Sam Tree", directors.get(0).getName());

        assertEquals(1, movie.getGenres().size());
        assertTrue(movie.getGenres().stream().anyMatch(g -> g.getName().equals("comedy")));

        assertEquals("Denmark", movie.getProductionCountry().getName());

    }

    @Test
    void shouldNotSaveDuplicateMovie() {
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setId(1);
        movieDTO.setTitle("copy Movie");
        movieDTO.setReleaseDate(LocalDate.of(2026, 9, 20));

        CreditsDTO creditsDTO = new CreditsDTO();
        creditsDTO.setCast(List.of());
        creditsDTO.setCrew(List.of());

        MovieDTO saved1 = movieService.saveMovie(movieDTO, creditsDTO);
        MovieDTO saved2 = movieService.saveMovie(movieDTO, creditsDTO);

        assertNotNull(saved1);
        assertNotNull(saved2);

        assertEquals("copy Movie", saved2.getTitle());

        List<Movie> allMovies = movieDAO.getAll();
        assertEquals(1, allMovies.size());

        Movie movie = movieDAO.getMovieByTmdbId(1);
        assertNotNull(movie);
        assertEquals("copy Movie", movie.getTitle());
    }

    @Test
    void shouldFindDirectorFromCredits() {
        CrewDTO directorCrew = new CrewDTO();
        directorCrew.setId(1);
        directorCrew.setName("Dolan Duck");
        directorCrew.setJob("Director");

        CreditsDTO creditsDTO = new CreditsDTO();
        creditsDTO.setCrew(List.of(directorCrew));

        DirectorDTO director = movieService.findDirector(creditsDTO);

        assertNotNull(director);
        assertEquals(1, director.getId());
        assertEquals("Dolan Duck", director.getName());
        assertEquals("Director", director.getJob());
    }

    @Test
    void shouldConvertDTOToEntity() {

        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setId(1);
        movieDTO.setTitle("Bottles Man");
        movieDTO.setReleaseDate(LocalDate.of(2026, 9, 20));

        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setName("Drama");
        movieDTO.setGenres(List.of(genreDTO));

        ProductionCountryDTO countryDTO = new ProductionCountryDTO();
        countryDTO.setName("Denmark");
        movieDTO.setProductionCountries(List.of(countryDTO));

        Movie movie = movieService.toEntity(movieDTO);

        assertNotNull(movie);
        assertEquals(1, movie.getTmdbId());
        assertEquals("Bottles Man", movie.getTitle());
        assertEquals(LocalDate.of(2026, 9, 20), movie.getReleaseDate());

        assertNotNull(movie.getGenres());
        assertEquals(1, movie.getGenres().size());
        assertTrue(movie.getGenres().stream().anyMatch(g -> g.getName().equals("Drama")));

        assertNotNull(movie.getProductionCountry());
        assertEquals("Denmark", movie.getProductionCountry().getName());


    }

    @Test
    void shouldConvertEntityToDTO() {
        Movie movie = new Movie();
        movie.setTitle("Curtains");
        movie.setReleaseDate(LocalDate.of(2026, 9, 20));

        Genre genre = new Genre();
        genre.setName("Drama");
        movie.setGenres(Set.of(genre));

        ProductionCountry country = new ProductionCountry();
        country.setName("Denmark");
        movie.setProductionCountry(country);

        movieDTO = movieService.toDTO(movie);

        assertNotNull(movieDTO);
        assertEquals("Curtains", movieDTO.getTitle());
        assertEquals(LocalDate.of(2026, 9, 20), movieDTO.getReleaseDate());

        assertEquals(1, movieDTO.getGenres().size());
        assertEquals("Drama", movieDTO.getGenres().get(0).getName());

        assertEquals(1, movieDTO.getProductionCountries().size());
        assertEquals("Denmark", movieDTO.getProductionCountries().get(0).getName());
    }

    @Test
    void shouldMapCastDTOToEntity() {

        Cast cast = movieService.castDTOToEntity(actor1);

        assertNotNull(cast);
        assertEquals(actor1.getId(), cast.getTmdbId());
        assertEquals(actor1.getName(), cast.getName());
    }

    @Test
    void shouldMapGenreDTOToEntity() {

        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setName("Comedy");

        Genre genre = movieService.genreDTOToEntity(genreDTO);

        assertNotNull(genre);
        assertEquals("Comedy", genre.getName());
    }

    @Test
    void shouldMapProductionCountryDTOToEntity() {

        ProductionCountryDTO countryDTO = new ProductionCountryDTO();
        countryDTO.setName("Denmark");

        ProductionCountry country = movieService.productionCountryDTOToEntity(countryDTO);

        assertNotNull(country);
        assertEquals("Denmark", country.getName());
    }

}