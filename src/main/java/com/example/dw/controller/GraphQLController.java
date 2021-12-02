package com.example.dw.controller;

import com.example.dw.provider.GraphQLProvider;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;

@Path("/graphql")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class GraphQLController {

    @Inject
    private GraphQLProvider graphQLProvider;

    public GraphQLController() {
    }

    @POST
    public Object post(
            @HeaderParam(HttpHeaders.CONTENT_TYPE) Optional<String> contentType,
            @Context UriInfo uriInfo,
            String body
    ) throws ParseException {
        log.info("Received GraphQL Query: {}", body);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(body);
        //return graphQLProvider.invoke(json.getAsString("query"));
        return graphQLProvider.invokePerRequest(json.getAsString("query"));
    }
}
