package app.DTOs;

import app.entities.Director;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)

public class MovieDTO {

    private int id;
    private String title;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    private List<GenreDTO> genres;

    @JsonProperty("production_countries")
    private List<ProductionCountryDTO> productionCountries;

}
