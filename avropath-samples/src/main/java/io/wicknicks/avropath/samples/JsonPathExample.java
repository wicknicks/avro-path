package io.wicknicks.avropath.samples;

import com.jayway.jsonpath.JsonPath;

public class JsonPathExample {

    static String json = "{\n" +
            "   \"store\":\n" +
            "    {\n" +
            "      \"book\":\n" +
            "      [\n" +
            "        {\n" +
            "            \"category\": \"reference\",\n" +
            "            \"author\": \"Nigel Rees\",\n" +
            "            \"title\": \"Sayings of the Century\",\n" +
            "            \"price\": 8.95\n" +
            "        },\n" +
            "        {\n" +
            "            \"category\": \"fiction\",\n" +
            "            \"author\": \"Evelyn Waugh\",\n" +
            "            \"title\": \"Sword of Honour\",\n" +
            "            \"price\": 12.99\n" +
            "        }\n" +
            "      ],\n" +
            "      \"bicycle\":\n" +
            "      {\n" +
            "          \"color\": \"red\",\n" +
            "          \"price\": 19.95\n" +
            "      }\n" +
            "    }\n" +
            "}";


    public static void main(String[] args) {
        System.out.println(json);
        Object o = JsonPath.read(json, "$.store.book[1].author");
        System.out.println(o);
        o = JsonPath.read(json, "$.store.bicycle.color");
        System.out.println(o);
    }
}
