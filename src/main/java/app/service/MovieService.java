package app.service;

import app.DTOs.*;
import app.Deserialization.Deserialization;
import app.entities.Genre;
import app.entities.Movie;
import app.entities.ProductionCountry;
import app.DAOs.MovieDAO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MovieService {

    private final MovieDAO movieDAO;

    public MovieService() {
        this.movieDAO = null;
    }

    // ______________________\\
    // ----- Get Movies -----\\

    public List<MovieDTO> getAllMovies() {

        return movieDAO.getAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<MovieDTO> getMoviesByTitle(String title) {

        return movieDAO.getMovieByTitle(title)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<MovieDTO> getMoviesByProductionCountry(String country) {

        return movieDAO.getByProductionCountry(country)
                .stream()
                .map(this::toDTO)
                .toList();
    }


    // -----------------------\\
    // ----- Save Movies -----\\

    public MovieDTO saveMovie(MovieDTO movieDTO) {

        Movie movie = toEntity(movieDTO);

        Movie savedMovie = movieDAO.saveMovie(movie);

        return toDTO(savedMovie);
    }

    public List<MovieDTO> saveMovies(List<MovieDTO> movieDTOs) {

        List<Movie> movies = movieDTOs
                .stream()
                .map(this::toEntity)
                .toList();

        return movieDAO.saveMovies(movies)
                .stream()
                .map(this::toDTO)
                .toList();
    }


    // __________________________ \\
    // ----- DTO to Entitiy ----- \\

    private Movie toEntity(MovieDTO dto) {

        Movie movie = new Movie();

        movie.setTitle(dto.getTitle());
        movie.setReleaseDate(dto.getReleaseDate());

        if (dto.getGenres() != null) {

            Set<Genre> genres = dto.getGenres()
                    .stream()
                    .map(this::genreDTOToEntity)
                    .collect(Collectors.toSet());

            movie.setGenres(genres);
        }

        if (dto.getProductionCountries() != null) {

            ProductionCountry country =
                    dto.getProductionCountries()
                            .stream()
                            .filter(c -> "DK".equalsIgnoreCase(c.getIsoCode()))
                            .findFirst()
                            .map(this::productionCountryDTOToEntity)
                            .orElse(null);

            movie.setProductionCountry(country);
        }

        return movie;
    }


    private Genre genreDTOToEntity(GenreDTO dto) {

        Genre existingGenre =
                movieDAO.getGenreByName(dto.getName());

        if (existingGenre != null) {
            return existingGenre;
        }

        Genre genre = new Genre();

        genre.setName(dto.getName());

        return movieDAO.saveGenre(genre);
    }


    private ProductionCountry productionCountryDTOToEntity(
            ProductionCountryDTO dto) {

        ProductionCountry existingCountry =
                movieDAO.getProductionCountryByName(
                        dto.getName()
                );

        if (existingCountry != null) {
            return existingCountry;
        }

        ProductionCountry country =
                new ProductionCountry();

        country.setName(dto.getName());

        return movieDAO.saveProductionCountry(country);
    }


    // ------------------------- \\
    // ----- Entity to DTO ----- \\

private MovieDTO toDTO(Movie movie) {

    MovieDTO dto = new MovieDTO();

    dto.setTitle(movie.getTitle());
    dto.setReleaseDate(movie.getReleaseDate());

    if (movie.getGenres() != null) {

        List<GenreDTO> genres = movie.getGenres()
                .stream()
                .map(this::toGenreDTO)
                .toList();

        dto.setGenres(genres);
    }

    if (movie.getProductionCountry() != null) {

        ProductionCountryDTO countryDTO =
                toProductionCountryDTO(
                        movie.getProductionCountry()
                );

        dto.setProductionCountries(
                List.of(countryDTO)
        );
    }


    return dto;
}

private GenreDTO toGenreDTO(Genre genre) {

    GenreDTO dto = new GenreDTO();

    dto.setId(genre.getId());
    dto.setName(genre.getName());

    return dto;
}

private ProductionCountryDTO toProductionCountryDTO(
        ProductionCountry country) {

    ProductionCountryDTO dto =
            new ProductionCountryDTO();

    dto.setName(country.getName());

    return dto;
}

     //______________________________\\
    // ----- Fetching From TMBd ----- \\

    public MovieDTO fetchMovie(int id) throws Exception {
        String apiKey = System.getenv("apiKey");
        String url = "https://api.themoviedb.org/3/movie/" + id + "?api_key=" + apiKey;

        HttpClient client = HttpClient.newHttpClient().newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Deserialization deserialization = new Deserialization();
        return deserialization.convertMovie(response.body());
    }

    public CreditsDTO fetchCredits(int id) throws Exception {
        String apiKey = System.getenv("apiKey");
        String url = "https://api.themoviedb.org/3/movie/" + id + "/credits?api_key=" + apiKey;

        HttpClient client = HttpClient.newHttpClient().newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Deserialization deserialization = new Deserialization();

        return deserialization.convertCredits(response.body(), CreditsDTO.class);
    }

    public DirectorDTO findDirector(CreditsDTO credits) {
        return credits.getCrew().stream().filter(c -> "Director".equals(c.getJob())).map(c -> new DirectorDTO(c.getId(), c.getName(), c.getJob())).findFirst().orElse(null);
    }

    public void printMovieAndCredits(int id) throws Exception {
        MovieDTO movieDTO = fetchMovie(id);
        CreditsDTO creditsDTO = fetchCredits(id);
        DirectorDTO directorDTO = findDirector(creditsDTO);

        System.out.println("Title: " + movieDTO.getTitle());
        System.out.println("Release date: " + movieDTO.getReleaseDate());

        System.out.println("Genres:");
        for (GenreDTO g : movieDTO.getGenres()) {
            System.out.println(" - " + g.getName());
        }

        System.out.println("Production countries:");
        for (ProductionCountryDTO pc : movieDTO.getProductionCountries()) {
            System.out.println(" - " + pc.getName());
        }

        if (directorDTO != null) {
            System.out.println("\n - " + directorDTO.getName() + " "+ directorDTO.getJob());
        }

        System.out.println("\nCast:");
        for (CastDTO c : creditsDTO.getCast()) {
            System.out.println(" - " + c.getName() + " as " + c.getCharacter());
        }


    }
}