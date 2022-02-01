# graphqlAppEclipseLinkMysql

Java - JDK 8+\
Framework - Dropwizard\
JPA - EclipseLink\
DB - MySql\
DI - Guice\
GraphQL - graphql-java

1. Entities\
Basic Entities Author and Book are used.\
Entities have OneToMany relationship i.e, one Author can own multiple Books.\
They are mapped to corresponding tables in Mysql DB.
OneToMany relationship annotation adds AUTH_id column into Book table at runtime.

2. Dao \
Data Access Objects (DAO) has basic methods to operate on entities or tables.

3. Resources and Service \
AuthorService and BookService serves /author and /book endpoints respectively and offers above mentioned DAO ops.\
A special operation known as 'findAuthorByIds' is added that fetches multiple Authors from datasource (DB) given a list of Author IDs.\
/graphql endpoint is added is served by GraphQLController.

4. GraphQL Provider and Data Fetchers \
Takes care of parsing graphql-schema and creating typeDefs out of it.\
RuntimeWiring is created by mapping Types to Datafetchers.\
Then the typeDefs and runtimeWiring are used to create a graphQLSchema.\
GraphQLSchema thus created is bound to a graphQL instance.\
In the init, dataloaders are registered and the same are used in graphQL execution.\
There are 2 data fetchers to get author data, one uses data loader and the other one without data loader.

5. EclipseLink \
A persistence.xml config is added in resources and has required config to create tables (if not present).

6. To start with: \
Build the project:
```
mvn clean install
```

Then run the executable:
```
java -jar target/graphqlAppEclipseLinkMysql-1.0-SNAPSHOT.jar server conf.yml
```

Then create author and book resources.
```
POST http://localhost:8080/author

Sample Payload:
{
    "name": "Bob",
    "age": 26
}
```

```
POST http://localhost:8080/book

Sample Payload:
{
    "description": "Programming in C",
    "title": "C",
    "authorId": 1
}
```

Add such multiple objects.

7. GraphQL query without data loader:
```
{
  books {
    title
    authorId
    author {
      title
    }
  }
}
```

Logs:
```
Body: {"operationName":null,"variables":{},"query":"{\n  books {\n    title\n    author {\n      age\n    }\n  }\n}\n"}
Registered data loaders
Invoking GraphQL Query
Using Registered Data Loaders
[EL Fine]: sql: 2021-11-13 20:11:02.453--ServerSession(1392339830)--Connection(832041998)--SELECT id, authorId, description, title, AUTH_id FROM book ORDER BY id
[EL Fine]: sql: 2021-11-13 20:11:02.484--ServerSession(1392339830)--Connection(832041998)--SELECT id, age, name FROM author WHERE (id = ?)
        bind => [1 parameter bound]
[EL Fine]: sql: 2021-11-13 20:11:02.493--ServerSession(1392339830)--Connection(832041998)--SELECT id, age, name FROM author WHERE (id = ?)
        bind => [1 parameter bound]
[EL Fine]: sql: 2021-11-13 20:11:02.497--ServerSession(1392339830)--Connection(832041998)--SELECT id, age, name FROM author WHERE (id = ?)
        bind => [1 parameter bound]
```

GraphQL instance with dataloaders registered:
```
Body: {"operationName":null,"variables":{},"query":"{\n  books {\n    title\n    author {\n      name\n    }\n  }\n}\n"}
Registered data loaders
Invoking GraphQL Query
Using Registered Data Loaders
[EL Fine]: sql: 2021-11-15 09:36:25.365--ServerSession(1392339830)--Connection(764423036)--SELECT id, authorId, description, title, AUTH_id FROM book ORDER BY id
[EL Fine]: sql: 2021-11-15 09:36:25.409--ServerSession(1392339830)--Connection(764423036)--SELECT id, age, name FROM author WHERE (id IN (?,?,?))
        bind => [3 parameters bound]
[0:0:0:0:0:0:0:1] - - [15/Nov/2021:04:06:25 +0000] "POST /graphql HTTP/1.1" 200 192 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36" 578
```

8. GraphQL query with filter:
```
query {
  booksWithFilter(
    filters: {
    	priceGt:200
    })
 {
    id
    title
    price
  }
}
```

Output:
```
{
  "errors": [],
  "data": {
    "booksWithFilter": [
      {
        "id": "3",
        "title": "JavaScript Programming",
        "description": "Programming in JS",
        "price": 500,
        "author": {
          "id": "3",
          "name": "Jim",
          "age": 35
        }
      }
    ]
  },
  "dataPresent": true
}
```

9. Query with basic pagination:
```
query {
  booksWithFilter(
    filters: {
        priceGt:50
    },
    pagination:{
        limit:1,
        offset:0
    })
 {
    id
    title
    price
  }
}
```

Output:
```
{
  "errors": [],
  "data": {
    "booksWithFilter": [
      {
        "id": "1",
        "title": "C Programming",
        "price": 200
      }
    ]
  },
  "dataPresent": true
}
```

10. Aggregation Example
```
query {
  booksAggregator(
    aggregation: {
        type:AVERAGE
        field:"price"
    }
  )
}
```

Output:
```
{
  "errors": [],
  "data": {
    "booksAggregator": 333
  },
  "dataPresent": true
}
```

11. Create
```
mutation createBook{
  createBook(filters: {
    	title:"Kubernetes Developer", 
      description:"Kubernetes Certification",
    	price:1500,
    	authorId:3
  }) {
    id
    title
    description
    price
  }
}
  
{
  "errors": [],
  "data": {
    "createBook": {
      "id": "5",
      "title": "Kubernetes Developer",
      "description": "Kubernetes Certification",
      "price": 1500
    }
  },
  "dataPresent": true
}  
```

Get the list of books again:
```
query listall {
	books {
    id
    title
    authorId
    description
    price
		author {
      age
      name
    }
  }
}

{
  "errors": [],
  "data": {
    "books": [
      {
        "id": "1",
        "title": "C Programming",
        "authorId": 1,
        "description": "Programming in C",
        "price": 200,
        "author": {
          "age": 25,
          "name": "Bob"
        }
      },
      {
        "id": "2",
        "title": "Java Programming",
        "authorId": 2,
        "description": "Programming in Java",
        "price": 300,
        "author": {
          "age": 30,
          "name": "Sam"
        }
      },
      {
        "id": "3",
        "title": "JavaScript Programming",
        "authorId": 3,
        "description": "Programming in JS",
        "price": 500,
        "author": {
          "age": 35,
          "name": "Jim"
        }
      },
      {
        "id": "4",
        "title": "GraphQL hands on",
        "authorId": 2,
        "description": "GraphQL API",
        "price": 500,
        "author": {
          "age": 30,
          "name": "Sam"
        }
      },
      {
        "id": "5",
        "title": "Kubernetes Developer",
        "authorId": 3,
        "description": "Kubernetes Certification",
        "price": 1500,
        "author": {
          "age": 35,
          "name": "Jim"
        }
      }
    ]
  },
  "dataPresent": true
}
```

12. Update
```
Before:
      {
        "id": "4",
        "title": "GraphQL hands on",
        "authorId": 2,
        "description": "GraphQL API",
        "price": 500,
        "author": {
          "age": 30,
          "name": "Sam"
        }
      },

Query:
mutation updateBook{
  updateBook(filters: {
    	id:4
    	title:"GraphQL Practical", 
      description:"GraphQL API developer",
    	price:1200,
    	authorId:3
  }) {
    id
    title
    description
    price
  }
}

Get:
{
  books {
    id
    title
    price
    description
    author {
      name
      age
    }
  }
}

After:
      {
        "id": "4",
        "title": "GraphQL Practical",
        "price": 1200,
        "description": "GraphQL API developer",
        "author": {
          "name": "Jim",
          "age": 35
        }
      },
```

13. Delete
```
mutation deleteBook{
  deleteBook(filters: {
    	id:4
  }) {
    id
  }
}
```

14. Interfaces:
```
query {
  items {
    	__typename
	id
    	title
    	... on Book {
          price
      	}
    	... on Author {
       	  age
      }
  }
}
```
And
```
query {
  item(id:1) {
    __typename
    id
    title
    ... on Book {
      price
    }
    ... on Author {
      age
    }
  }
}
```

15. Unions:
```
query {
  inventory {
    	__typename
    	... on Book {
          id
          title
          description
          price
        }
    	... on Author {
          id
          title
          age
        }
  }
}
```
And
```
query {
  inventoryItem(id:1) {
    ... on Book {
      id
      title
      price
    }
    ... on Author {
      id
      title
      age
    }
  }
}
```

In depth Data Loader Analysis:
Query:
```
{
  books {
    id
    title
    authorId
    author {
      title
    }
    description
    countryOfOrigin
  }
}
```

1. Without dataloader:
```
2022-02-01T07:02:27.919450Z	   55 Query	SET SQL_SELECT_LIMIT=DEFAULT
2022-02-01T07:02:27.921998Z	   55 Query	SELECT id, authorId, description, price, title, AUTH_id FROM book ORDER BY id
2022-02-01T07:02:27.926987Z	   55 Query	SHOW WARNINGS
2022-02-01T07:02:27.945256Z	   55 Query	SELECT id, authorId, description, price, title, AUTH_id FROM book ORDER BY id
2022-02-01T07:02:27.949239Z	   55 Query	SHOW WARNINGS
2022-02-01T07:02:28.058968Z	   55 Query	SELECT id, age, title FROM author WHERE (id = 1)
2022-02-01T07:02:28.061982Z	   55 Query	SHOW WARNINGS
2022-02-01T07:02:28.069665Z	   55 Query	SELECT id, age, title FROM author WHERE (id = 2)
2022-02-01T07:02:28.071944Z	   55 Query	SHOW WARNINGS
2022-02-01T07:02:28.076526Z	   55 Query	SELECT id, age, title FROM author WHERE (id = 3)
2022-02-01T07:02:28.080367Z	   55 Query	SHOW WARNINGS
```

2. With Dataloader:
```
2022-02-01T06:53:10.246826Z	   49 Query	SELECT id, authorId, description, price, title, AUTH_id FROM book ORDER BY id
2022-02-01T06:53:10.250720Z	   49 Query	SHOW WARNINGS
2022-02-01T06:53:10.255832Z	   49 Query	SELECT id, authorId, description, price, title, AUTH_id FROM book ORDER BY id
2022-02-01T06:53:10.261577Z	   49 Query	SHOW WARNINGS
2022-02-01T06:53:10.302831Z	   49 Query	SELECT id, age, title FROM author WHERE (id IN (1,2,3))
2022-02-01T06:53:10.306298Z	   49 Query	SHOW WARNINGS
```

3. With Left Join in the resolver itself:
```

```
