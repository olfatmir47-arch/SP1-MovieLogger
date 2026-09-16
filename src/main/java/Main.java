import DTOs.GenreDTO;
import DTOs.MovieDTO;
import DTOs.ProductionCountryDTO;
import Service.MovieService;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("HELLO FROM MAIN");
        MovieService movieService = new MovieService();
        movieService.printMovieAndCredits(11);
    }
}
