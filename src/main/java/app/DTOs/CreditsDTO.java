package app.DTOs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)

public class CreditsDTO {
    private int id;
    private List<CastDTO> cast;
    private List<CrewDTO> crew;
    private List<DirectorDTO> director;
}
