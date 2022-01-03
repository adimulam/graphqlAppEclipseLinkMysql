package com.example.dw.datafetchers;

import java.util.HashMap;
import java.util.Map;

public interface ResolverContract {
    static Map<String, Object> wrapScalarResult(Number result) {
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("result", result);
        return resMap;
    }
}
