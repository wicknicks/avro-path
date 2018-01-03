package io.wicknicks.avropath.core;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.JsonProvider;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AvroProvider implements JsonProvider {

    @Override
    public Object parse(String json) throws InvalidJsonException {
        return parse(new ByteArrayInputStream(json.getBytes()), "UTF-8");
    }

    @Override
    public Object parse(InputStream jsonStream, String charset) throws InvalidJsonException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            int nRead;
            while ((nRead = jsonStream.read()) != -1) {
                bos.write(nRead);
            }
        } catch (IOException e) {
            throw new InvalidJsonException("Could not read bytes", e);
        }

        try {
            DataFileReader<GenericRecord> reader = new DataFileReader<>(new SeekableByteArrayInput(bos.toByteArray()), new GenericDatumReader<GenericRecord>());
            return reader.next();
        } catch (IOException e) {
            throw new InvalidJsonException("Could not decode avro", e);
        }
    }

    @Override
    public String toJson(Object obj) {
        return obj.toString();
    }

    @Override
    public Object createArray() {
        return new LinkedList<>();
    }

    @Override
    public Object createMap() {
        throw new JsonPathException("cannot create array");
    }

    @Override
    public boolean isArray(Object obj) {
        return obj instanceof List;
    }

    @Override
    public int length(Object obj) {
        if (isArray(obj)) {
            return ((List) obj).size();
        } else if (isMap(obj)) {
            throw new InvalidJsonException("Not yet implemented: length(obj) if obj is Map");
        } else if (obj instanceof String) {
            return ((String) obj).length();
        }
        throw new JsonPathException("Cannot get length for class: " + obj.getClass().getName());
    }

    @Override
    public Iterable<?> toIterable(Object obj) {
        if (isArray(obj))
            return ((Iterable) obj);
        else
            throw new JsonPathException("Cannot iterate over " + obj.getClass().getName());
    }

    @Override
    public Collection<String> getPropertyKeys(Object obj) {
        if (isArray(obj)) {
            throw new UnsupportedOperationException("Cannot get property keys for an array");
        } else if (obj instanceof GenericContainer){
            GenericContainer rec = (GenericContainer) obj;
            return rec.getSchema().getFields().stream().map(Schema.Field::name).collect(Collectors.toList());
        } else {
            throw new JsonPathException("Cannot interpret class " + obj.getClass().getName());
        }
    }

    @Override
    public Object getArrayIndex(Object obj, int idx) {
        return ((List) obj).get(idx);
    }

    @Override
    public Object getArrayIndex(Object obj, int idx, boolean unwrap) {
        return getArrayIndex(obj, idx);
    }

    @Override
    public void setArrayIndex(Object array, int idx, Object newValue) {
        if (!isArray(array)) {
            throw new UnsupportedOperationException();
        } else {
            List l = (List) array;
            if (idx == l.size()){
                l.add(newValue);
            }else {
                l.set(idx, newValue);
            }
        }
    }

    @Override
    public Object getMapValue(Object obj, String key) {
        if (obj instanceof IndexedRecord) {
            IndexedRecord rec = (IndexedRecord) obj;
            Schema.Field f = rec.getSchema().getField(key);
            if (f == null) return JsonProvider.UNDEFINED;
            else return rec.get(f.pos());
        } else {
            return JsonProvider.UNDEFINED;
        }
    }

    @Override
    public void setProperty(Object obj, Object key, Object value) {
        if (isMap(obj)) {
            IndexedRecord rec = (IndexedRecord) obj;
            Schema.Field field = rec.getSchema().getField(String.valueOf(key));
            if (field == null) throw new InvalidJsonException("No such field in schema: " + key);
            rec.put(field.pos(), value);
        } else if (isArray(obj)) {
            setArrayIndex(obj, key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString()), value);
        } else {
            throw new InvalidJsonException("Not yet implemented: setProperty(Object obj, Object key, Object value)");
        }
    }

    public void removeProperty(Object obj, Object key) {
        throw new UnsupportedOperationException("Cannot remove fields.");
    }

    @Override
    public boolean isMap(Object obj) {
        return obj instanceof IndexedRecord;
    }

    @Override
    public Object unwrap(Object obj) {
        return obj;
    }

}
