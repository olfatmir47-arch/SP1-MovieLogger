package app.DAOs;

import app.entities.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class MovieDAO {

    private final EntityManagerFactory emf;

    public MovieDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }


    // ----- All The Getting -----
    public List<Movie> getAll() {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT m FROM Movie m",
                    Movie.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public List<Movie> getByProductionCountry(String country) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery("""
                    SELECT m
                    FROM Movie m
                    JOIN m.productionCountry pc
                    WHERE LOWER(pc.name) = LOWER(:country)
                    """, Movie.class)
                    .setParameter("country", country)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Movie> getMovieByTitle(String title) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery("""
                    SELECT m
                    FROM Movie m
                    WHERE LOWER(m.title) LIKE LOWER(:title)
                    """, Movie.class)
                    .setParameter("title", "%" + title + "%")
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Actor> getAllActorsByMovieTitle(Movie movie) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery("""
                    SELECT a
                    FROM Movie m
                    JOIN m.actors a
                    WHERE m.id = :movieId
                    """, Actor.class)
                    .setParameter("movieId", movie.getId())
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Director> getAllDirectorsByMovieTitle(Movie movie) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery("""
                    SELECT d
                    FROM Movie m
                    JOIN m.directors d
                    WHERE m.id = :movieId
                    """, Director.class)
                    .setParameter("movieId", movie.getId())
                    .getResultList();

        } finally {
            em.close();
        }
    }

    //______________________________________________
    //----- Movie Saving, Updating and Deleting-----

    public Movie saveMovie(Movie movie) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(movie);

            em.getTransaction().commit();

            return movie;

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public List<Movie> saveMovies(List<Movie> movies) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            for (Movie movie : movies) {
                em.persist(movie);
            }

            em.getTransaction().commit();

            return movies;

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public Movie updateMovie(Movie movie) {

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Movie updatedMovie = em.merge(movie);

            em.getTransaction().commit();

            return updatedMovie;

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public void deleteMovie(int id) {

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Movie movie = em.find(Movie.class, id);

            if (movie != null) {
                em.remove(movie);
            }

            em.getTransaction().commit();

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    // ____________________________ \\
    // ----- replica Security ----- \\

    public Genre getGenreByName(String name) {

        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery("""
                    SELECT g
                    FROM Genre g
                    WHERE LOWER(g.name) = LOWER(:name)
                    """, Genre.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            em.close();
        }
    }


    public Genre saveGenre(Genre genre) {

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(genre);

            em.getTransaction().commit();

            return genre;

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public ProductionCountry getProductionCountryByName(String name) {

        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery("""
                    SELECT p
                    FROM ProductionCountry p
                    WHERE LOWER(p.name) = LOWER(:name)
                    """, ProductionCountry.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            em.close();
        }
    }


    public ProductionCountry saveProductionCountry(
            ProductionCountry country) {

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(country);

            em.getTransaction().commit();

            return country;

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }



}

