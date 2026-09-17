import app.service.MovieService;

public class Main {
    public static void main(String[] args) throws Exception {

        MovieService movieService = new MovieService();

        movieService.printMovieAndCredits(11);
    }
}