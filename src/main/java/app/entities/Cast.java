package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "movie_cast_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private int tmdbId;

    private String name;

    @ManyToMany(mappedBy = "cast")
    private Set<Movie> movies;
}