package app.config;

import org.hibernate.cfg.Configuration;


public class EntityRegistry {
    private EntityRegistry(){}
    static void registerEntities(Configuration configuration){
        //configuration.addAnnotatedClass(Point.class);
    }
}
