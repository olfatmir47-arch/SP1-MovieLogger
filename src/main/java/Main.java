import app.DTOs.CreditsDTO;
import app.DTOs.MovieDTO;
import app.service.MovieService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

    public class Main {

        public static void main(String[] args) throws Exception {

            EntityManagerFactory emf =
                    Persistence.createEntityManagerFactory("MovieLoggerPU");

            MovieService movieService = new MovieService(emf);

            int page = 1;
            int maxPages = 100;

            for (int i = 0; i < maxPages; i++){
                List<MovieDTO> movies =
                        movieService.fetchMovieByCountry(
                                "DK",
                                "2021-09-17",
                                "2026-09-17",
                                page
                                );

            System.out.println("Found " + movies.size() + " movies.");

            for (MovieDTO movieDTO : movies) {

                MovieDTO fullMovie =
                        movieService.fetchMovie(movieDTO.getId());

                CreditsDTO creditsDTO =
                        movieService.fetchCredits(fullMovie.getId());

                movieService.saveMovie(fullMovie, creditsDTO);

                System.out.println(
                        "Saved: " + fullMovie.getTitle()
                );
            }
                page++;
            }
            emf.close();
        }
    }