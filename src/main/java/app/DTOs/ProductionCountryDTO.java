package app.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ProductionCountryDTO {
    @JsonProperty("iso_3166_1")
    private String isoCode;

    private String name;

}
