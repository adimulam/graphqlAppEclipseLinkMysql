package com.example.dw;

import com.example.dw.config.Config;
import com.example.dw.config.DbConfig;
import com.example.dw.controller.GraphQLController;
import com.example.dw.resource.HomeResource;
import com.example.dw.resource.BookResource;
import com.example.dw.resource.AuthorResource;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Properties;

public class graphqlApp extends Application<Config> {

    public static void main(String[] args) throws Exception {
        new graphqlApp().run(args);
    }

    @Override
    public void initialize(final Bootstrap<Config> bootstrap) {
    }

    @Override
    public void run(final Config conf, final Environment env) throws Exception {
        final Injector injector = Guice.createInjector(new graphqlAppModule(conf, env), createJpaModule(conf.getDbConfig()));
        env.servlets().addFilter("persistFilter", injector.getInstance(PersistFilter.class));
        env.jersey().register(injector.getInstance(HomeResource.class));
        env.jersey().register(injector.getInstance(BookResource.class));
        env.jersey().register(injector.getInstance(AuthorResource.class));
        env.jersey().register(injector.getInstance(GraphQLController.class));
    }

    private JpaPersistModule createJpaModule(final DbConfig dbConfig) {
        final Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.driver", dbConfig.getDriver());
        properties.put("javax.persistence.jdbc.url", dbConfig.getUrl());
        properties.put("javax.persistence.jdbc.user", dbConfig.getUsername());
        properties.put("javax.persistence.jdbc.password", dbConfig.getPassword());

        final JpaPersistModule jpaModule = new JpaPersistModule("DefaultUnit");
        jpaModule.properties(properties);

        return jpaModule;
    }
}