package com.example.dw.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataSource {
    private String name;
    private String type;
    private String url;
    private String username;
    private String password;
}
