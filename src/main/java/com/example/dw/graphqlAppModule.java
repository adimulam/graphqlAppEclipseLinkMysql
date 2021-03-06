package com.example.dw;

import com.example.dw.config.Config;
import com.example.dw.dao.Dao;
import com.example.dw.dao.DaoImpl;
import com.example.dw.datafetchers.GraphQLDataFetcher;
import com.example.dw.eclipselink.service.AuthorServiceEclipseLink;
import com.example.dw.eclipselink.service.BookServiceEclipseLink;
import com.example.dw.eclipselink.utils.EntityManagerFactoryUtil;
import com.example.dw.provider.GraphQLProvider;
import com.example.dw.service.BookService;
import com.example.dw.service.AuthorService;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import io.dropwizard.setup.Environment;

public class graphqlAppModule extends AbstractModule {

    final Config configuration;
    final Environment environment;

    public graphqlAppModule(final Config configuration, final Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(Config.class).toInstance(configuration);
        bind(Environment.class).toInstance(environment);
        bind(Dao.class).to(DaoImpl.class).in(Singleton.class);
        bind(BookService.class).in(Singleton.class);
        bind(AuthorService.class).in(Singleton.class);
        bind(EntityManagerFactoryUtil.class).in(Singleton.class);
        bind(BookServiceEclipseLink.class).in(Singleton.class);
        bind(AuthorServiceEclipseLink.class).in(Singleton.class);
        bind(GraphQLDataFetcher.class).in(Singleton.class);
        bind(GraphQLProvider.class).in(Singleton.class);
    }
}