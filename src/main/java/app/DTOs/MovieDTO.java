package app.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private String title;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    private List<GenreDTO> genres;

    @JsonProperty("production_countries")
    private List<ProductionCountryDTO> productionCountries;



}
