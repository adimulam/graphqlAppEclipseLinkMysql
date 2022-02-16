package com.example.dw.persistence;

import java.util.HashMap;
import java.util.Map;

public class PersistenceConstants {

   public static final Map<String,Class<?>> persistenceTypeMap = new HashMap<String, Class<?>>() {{
       put("String", String.class);
       put("int", int.class);
       put("Long", long.class);
       put("Boolean", Boolean.class);
       put("Char", char.class);
       put("Double", Double.class);
   }};

   public static final String JOIN_COLUMN = "JOIN_COLUMN";
   public static final String ENTITY_SELECTION_SET = "ENTITY_SELECTION_SET";
   public enum relationshipTypes  {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_MANY
    }


}
