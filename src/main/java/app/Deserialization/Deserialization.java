package app.Deserialization;


import app.DTOs.CreditsDTO;
import app.DTOs.MovieDTO;
import app.DTOs.MovieResultsDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Deserialization {
    ObjectMapper objectMapper = new ObjectMapper();

    public Deserialization(){
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public MovieDTO convertMovie(String json) {
        try {
           return objectMapper.readValue(json, MovieDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public CreditsDTO convertCredits(String json, Class<CreditsDTO> creditsDTOClass) {
        try {
            return objectMapper.readValue(json, CreditsDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public MovieResultsDTO convertMovies(String json) {
        try {
            return objectMapper.readValue(json, MovieResultsDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize movies", e);
        }
    }
}
