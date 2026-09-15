package DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public class MovieDTO {
    private String title;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    private List<GenreDTO> genres;

    @JsonProperty("production_countries")
    private List<ProductionCountryDTO> productionCountries;



}
