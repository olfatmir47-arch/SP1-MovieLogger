package DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductionCountryDTO {
    @JsonProperty("iso_3166_1")
    private int isoCode;

    private String name;

}
