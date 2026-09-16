package DTOs;

import java.util.List;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)

public class CreditsDTO {
    private int id;
    private List<CastDTO> cast;
    private List<DirectorDTO> crew;
}
