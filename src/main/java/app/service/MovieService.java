package app.service;

import app.DTOs.MovieDTO;
import app.DTOs.GenreDTO;
import app.DTOs.ProductionCountryDTO;
import app.entities.Genre;
import app.entities.Movie;
import app.entities.ProductionCountry;
import app.DAOs.MovieDAO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MovieService {

    private final MovieDAO movieDAO;

    public MovieService(MovieDAO movieDAO) {
        this.movieDAO = movieDAO;
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


    // Genres

    if (movie.getGenres() != null) {

        List<GenreDTO> genres = movie.getGenres()
                .stream()
                .map(this::toGenreDTO)
                .toList();

        dto.setGenres(genres);
    }


    // Production country

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
}