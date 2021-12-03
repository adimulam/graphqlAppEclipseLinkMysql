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
      name
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
