package Service;

import DTOs.*;
import app.Deserialization.Deserialization;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MovieService {

    public MovieDTO fetchMovie(int id) throws Exception {
        String apiKey = System.getenv("apiKey");
        String url = "https://api.themoviedb.org/3/movie/" + id + "?api_key=" + apiKey;

        HttpClient client = HttpClient.newHttpClient().newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Deserialization deserialization = new Deserialization();
        return deserialization.convertMovie(response.body());
    }

    public CreditsDTO fetchCredits(int id) throws Exception {
        String apiKey = System.getenv("apiKey");
        String url = "https://api.themoviedb.org/3/movie/" + id + "/credits?api_key=" + apiKey;

        HttpClient client = HttpClient.newHttpClient().newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Deserialization deserialization = new Deserialization();

        return deserialization.convertCredits(response.body(), CreditsDTO.class);
    }

    public DirectorDTO findDirector(CreditsDTO credits) {
        return credits.getCrew().stream().filter(c -> "Director".equals(c.getJob())).map(c -> new DirectorDTO(c.getId(), c.getName(), c.getJob())).findFirst().orElse(null);
    }

    public void printMovieAndCredits(int id) throws Exception {
        MovieDTO movieDTO = fetchMovie(id);
        CreditsDTO creditsDTO = fetchCredits(id);
        DirectorDTO directorDTO = findDirector(creditsDTO);

        System.out.println("Title: " + movieDTO.getTitle());
        System.out.println("Release date: " + movieDTO.getReleaseDate());

        System.out.println("Genres:");
        for (GenreDTO g : movieDTO.getGenres()) {
            System.out.println(" - " + g.getName());
        }

        System.out.println("Production countries:");
        for (ProductionCountryDTO pc : movieDTO.getProductionCountries()) {
            System.out.println(" - " + pc.getName());
        }

        if (directorDTO != null) {
            System.out.println("\n - " + directorDTO.getName() + " "+ directorDTO.getJob());
        }

        System.out.println("\nCast:");
        for (CastDTO c : creditsDTO.getCast()) {
            System.out.println(" - " + c.getName() + " as " + c.getCharacter());
        }


    }
}
