package io.wicknicks.avropath.samples;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import io.wicknicks.avropath.core.AvroProvider;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.*;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InjectProviders {

    private static final Logger log = LoggerFactory.getLogger(InjectProviders.class);

    public static void main(String[] args) throws IOException {
        Schema book = SchemaBuilder.record("book").fields()
                .requiredString("author").requiredString("title")
                .requiredDouble("price")
                .endRecord();

        Schema bicycle = SchemaBuilder.record("bicycle").fields()
                .requiredString("color").requiredDouble("price").endRecord();

        Schema schema = SchemaBuilder.record("request").fields()
                .name("some_int").type().intType().intDefault(0)
                .name("some_double").type().optional().doubleType()
                .name("some_string").type().nullable().stringType().stringDefault("")
                .name("books").type().array().items().type(book).noDefault()
                .name("bicycle").type(bicycle).noDefault()
                .endRecord();

        Schema store = SchemaBuilder.record("store").fields()
                .name("store").type().record("store_contents").fields()
                .name("some_int").type().intType().intDefault(0)
                .name("some_double").type().optional().doubleType()
                .name("some_string").type().nullable().stringType().stringDefault("")
                .name("books").type().array().items().type(book).noDefault()
                .name("bicycle").type(bicycle).noDefault().endRecord()
                .noDefault().endRecord();

        List<GenericRecord> bookArr = new ArrayList<>();
        bookArr.add(new GenericRecordBuilder(book).set("author", "ABC").set("title", "T1").set("price", 7.99).build());
        bookArr.add(new GenericRecordBuilder(book).set("author", "DEF").set("title", "T2").set("price", 8.50).build());

        GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schema);
        GenericRecord record = recordBuilder
                .set("some_int", 123)
                .set("some_string", null)
                .set("books", bookArr)
                .set("bicycle", new GenericRecordBuilder(bicycle).set("color", "red").set("price", 1079.85).build())
                .build();

        log.info("{}", record);

        GenericRecord storeRecord = new GenericRecordBuilder(store)
                .set("store", record)
                .build();

        log.info("store record: {}", storeRecord);

        File tmp = File.createTempFile("tmp", "avro.out");
        tmp.deleteOnExit();

        DataFileWriter<GenericRecord> writer = new DataFileWriter<>(new GenericDatumWriter<GenericRecord>());
        writer.create(store, tmp);
        writer.append(storeRecord);
        writer.close();

        DataFileReader<GenericRecord> reader = new DataFileReader<>(tmp, new GenericDatumReader<GenericRecord>());
        while (reader.hasNext()) {
            GenericRecord rec = reader.next();
            log.info("{}", rec);
        }

        Configuration conf = Configuration.defaultConfiguration().jsonProvider(new AvroProvider());
        log.info("$.store.books[1].author = {} ", (Object) JsonPath.parse(tmp, conf).read("$.store.books[1].author"));
        log.info("$.store.bicycle.color = {} ", (Object) JsonPath.parse(tmp, conf).read("$.store.bicycle.color"));
    }
}
