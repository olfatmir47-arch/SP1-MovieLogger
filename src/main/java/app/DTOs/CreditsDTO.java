package app.DTOs;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditsDTO {
    private int id;
    private List<ActorDTO> cast;
    private List<DirectorDTO> crew;
}
