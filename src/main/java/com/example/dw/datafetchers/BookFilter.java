package com.example.dw.datafetchers;

/*
import lombok.Data;

@Data
public class BookFilter {
    private FilterField price;
    private FilterField title;
}
*/
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BookFilter {
    private FilterField price;
    private FilterField title;

    @JsonProperty("price") //the name must match the schema
    public FilterField getPrice() {
        return price;
    }

    public void setPrice(FilterField price) {
        this.price = price;
    }

    @JsonProperty("title")
    public FilterField getTitle() {
        return title;
    }

    public void setTitle(FilterField title) {
        this.title = title;
    }
}