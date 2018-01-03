# avro-path

avro-path can be used to execute XPath like expressions on Avro documents. 

Given an Avro document such as the following: 

```
{
  "store": {
    "some_int": 123,
    "some_double": null,
    "some_string": null,
    "books": [
      {
        "author": "War and Peace",
        "title": "Leo Tolstoy",
        "price": 23.99
      },
      {
        "author": "Les Misérables",
        "title": "Victor Hugo",
        "price": 18.50
      }
    ],
    "bicycle": {
      "color": "red",
      "price": 1079.85
    }
  }
}
```

We can use XPath like expressions to pull out relevant pieces of Avro Data. For example: 

| Path Expression | Result |
| :------- | :----- |
|$.store.books[1].author | Victor Hugo |
|$.store.bicycle.color | red |

Avropath uses the JsonPath expression parser and evalutor from the Json-Path project 
(https://github.com/json-path/JsonPath). It simply provides a new Avro compatible adapter 
which allows the evaluator to unnest and process Avro documents. To use this, include 
avropath in your project along with json-path, and initialize JsonPath as follows:

```java
Configuration conf = Configuration.defaultConfiguration().jsonProvider(new AvroProvider());
Object result = JsonPath.parse(tmp, conf).read("$.store.books[1].author")
```
