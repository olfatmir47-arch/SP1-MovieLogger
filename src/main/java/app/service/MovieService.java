package app.service;

import app.DTOs.*;
import app.Deserialization.Deserialization;
import app.entities.*;
import app.DAOs.MovieDAO;
import jakarta.persistence.EntityManagerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class MovieService {

    private final MovieDAO movieDAO;

    public MovieService(EntityManagerFactory emf) {
        this.movieDAO = new MovieDAO(emf);
    }

    // -----------------------\\
    // ----- Save Movies -----\\

    public MovieDTO saveMovie(MovieDTO movieDTO, CreditsDTO creditsDTO) {

        Movie existingMovie =
                movieDAO.getMovieByTmdbId(movieDTO.getId());

        if (existingMovie != null) {
            System.out.println(
                    "Already exists: " + existingMovie.getTitle()
            );

            return movieDTO;
        }
        Movie movie = toEntity(movieDTO);

        addCreditsToMovie(movie, creditsDTO);

        Movie savedMovie = movieDAO.saveMovie(movie);
        System.out.println("Saving movie: " + movie.getTitle());
        System.out.println("TMDB ID: " + movie.getTmdbId());
        System.out.println("Cast:");

        for (Cast cast : movie.getCast()) {
            System.out.println(
                    "  Cast DB ID: " + cast.getId()
                            + " - " + cast.getName()
            );
        }
        return toDTO(savedMovie);
    }


    private void addCreditsToMovie(Movie movie, CreditsDTO creditsDTO){
        if(creditsDTO == null){
            return;
        }

        if (creditsDTO.getCast() != null) {

            Map<Integer, Cast> uniqueCasts = new LinkedHashMap<>();

            for (CastDTO dto : creditsDTO.getCast()) {
                Cast cast = castDTOToEntity(dto);
                uniqueCasts.put(cast.getId(), cast);
            }

            Set<Cast> casts = new HashSet<>(uniqueCasts.values());

            movie.setCast(casts);
        }

        DirectorDTO directorDTO = findDirector(creditsDTO);

        if (directorDTO != null) {

            Director director = directorDTOToEntity(directorDTO);

            movie.setDirectors(Set.of(director));;
        }
    }


    // __________________________ \\
    // ----- DTO to Entitiy ----- \\

    private Movie toEntity(MovieDTO dto) {

        Movie movie = new Movie();

        movie.setTmdbId(dto.getId());
        movie.setTitle(dto.getTitle());
        movie.setReleaseDate(dto.getReleaseDate());

        if (dto.getGenres() != null) {

            Set<Genre> genres = dto.getGenres()
                    .stream()
                    .map(this::genreDTOToEntity)
                    .collect(Collectors.toSet());

            movie.setGenres(genres);
        }

        if (dto.getProductionCountries() != null &&
                !dto.getProductionCountries().isEmpty()) {

            ProductionCountry country =
                    productionCountryDTOToEntity(
                            dto.getProductionCountries().get(0)
                    );

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
                movieDAO.getProductionCountryByName(dto.getName());

        if (existingCountry != null) {
            return existingCountry;
        }

        ProductionCountry country = new ProductionCountry();

        country.setName(dto.getName());

        return movieDAO.saveProductionCountry(country);
    }

    private Director directorDTOToEntity(DirectorDTO dto) {

        Director existingDirector =
                movieDAO.getDirectorByName(
                        dto.getName()
                );

        if (existingDirector != null) {
            return existingDirector;
        }
        Director director = new Director();

        director.setName(dto.getName());

        return movieDAO.saveDirector(director);
    }

    private Cast castDTOToEntity(CastDTO dto) {

        Cast existingCast =
                movieDAO.getCastByTmdbId(dto.getId());

        if (existingCast != null) {
            return existingCast;
        }

        Cast cast = new Cast();

        cast.setTmdbId(dto.getId());
        cast.setName(dto.getName());

        return movieDAO.saveActor(cast);
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

    //______________________________\\
    // ----- fetch by country ----- \\

    public List<MovieDTO> fetchMovieByCountry(String country, String fromDate, String toDate, int page) throws Exception {


        String apiKey = System.getenv("apiKey");

            String url = "https://api.themoviedb.org/3/discover/movie"
                + "?api_key=" + apiKey
                + "&with_origin_country=" + country
                + "&primary.release_date.gte=" + fromDate
                + "&primary.release_date.lte=" + toDate
                + "&page=" + page;

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            MovieResultsDTO movieResults =
                    new Deserialization().convertMovies(response.body());

            if (movieResults.getResults().isEmpty()) {
                throw new RuntimeException("No movies found for: " + country);
            }

        return movieResults.getResults()
                .stream()
                .limit(20)
                .toList();
    }




}