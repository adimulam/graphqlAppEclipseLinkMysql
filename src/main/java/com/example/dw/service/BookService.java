package com.example.dw.service;

import com.example.dw.datafetchers.ResolverContract;
import com.example.dw.entity.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableMap;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.util.*;

public class BookService extends AbstractService<Book> implements ResolverContract {

    public List<Book> findBookByTitle(final Optional<String> title) {
        if (title.isPresent()) {
            return dao.find(entityClass, "Book.findByTitle",
                    ImmutableMap.of("title", title.get()), 0);
        } else {
            return new ArrayList<>(0);
        }
    }

    private static JSONObject createJSONObject(String jsonString){
        JSONObject  jsonObject=new JSONObject();
        JSONParser jsonParser=new  JSONParser();
        if ((jsonString != null) && !(jsonString.isEmpty())) {
            try {
                jsonObject=(JSONObject) jsonParser.parse(jsonString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public List<Book> findWithAuthor() {
        return dao.findWithFilter(entityClass, "Book.findWithAuthor");
    }
    public List<Book> findBookByFilter(Object filter, Object pagination, Object distinct, Object sort) throws JsonProcessingException {
        int limit = 0;
        if (filter != null) {
            ObjectWriter filterObjWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String filterString = filterObjWriter.writeValueAsString(filter);
            JSONObject filterJson = createJSONObject(filterString);
            String fil = null;
            int price = 0;
            for (String ft : filterJson.keySet()) {
                fil = ft;
                price = (Integer) filterJson.get(fil);
            }
            if (pagination != null) {
                ObjectWriter paginationObjWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String paginationString = paginationObjWriter.writeValueAsString(pagination);
                JSONObject paginationJson = createJSONObject(paginationString);
                limit = (Integer) paginationJson.get("limit");
            }
            switch(Objects.requireNonNull(fil)) {
                case "priceLt":
                    return dao.find(entityClass, "Book.findByPriceLt", ImmutableMap.of("price", price ), limit);
                case "priceLe":
                    return dao.find(entityClass, "Book.findByPriceLe", ImmutableMap.of("price", price ), limit);
                case "priceGt":
                    return dao.find(entityClass, "Book.findByPriceGt", ImmutableMap.of("price", price ), limit);
                case "priceGe":
                    return dao.find(entityClass, "Book.findByPriceGe", ImmutableMap.of("price", price ), limit);
                case "priceEq":
                    return dao.find(entityClass, "Book.findByPriceEq", ImmutableMap.of("price", price ), limit);
            }
        }
        return null;
    }

    public Map<String,Object> findAggregation(Object aggregation) throws JsonProcessingException {
        String aggregationType = null;
        String aggregationField = null;
        if (aggregation != null) {
            ObjectWriter paginationObjWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String aggregationString = paginationObjWriter.writeValueAsString(aggregation);
            JSONObject aggregationJson = createJSONObject(aggregationString);
            aggregationType = (String)aggregationJson.get("type");
            aggregationField = (String)aggregationJson.get("field");
            Map<String, String> map = new HashMap<>();
            map.put("type", aggregationType);
            map.put("field", aggregationField);
            System.out.println(map);
            float result;
            switch(aggregationType.toLowerCase(Locale.ROOT)) {
                case "max":
                    result = dao.findAggregate(entityClass, "Book.findAggregateMax");
                    break;
                case "min":
                    result = dao.findAggregate(entityClass, "Book.findAggregateMin");
                    break;
                case "average":
                    result = dao.findAggregate(entityClass, "Book.findAggregateAvg");
                    break;
                case "sum":
                    result = dao.findAggregate(entityClass, "Book.findAggregateSum");
                    break;
                case "count":
                    result = dao.findAggregate(entityClass, "Book.findAggregateCount");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + aggregationType.toLowerCase(Locale.ROOT));
            }
            //Map<String, Object> resMap = new HashMap<>();
            //resMap.put("result", result);
            //return resMap;
            return ResolverContract.wrapScalarResult(result);
        }
        return null;
    }
}