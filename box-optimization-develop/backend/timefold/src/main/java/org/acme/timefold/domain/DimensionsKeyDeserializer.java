package org.acme.timefold.domain;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import java.io.IOException;

public class DimensionsKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        // Assuming the key is in the format "x,y,z"
        String[] parts = key.split(",");
        if (parts.length != 3) {
            throw new IOException("Invalid Dimensions format");
        }
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        int z = Integer.parseInt(parts[2].trim());
        return new Dimensions(x, y, z);
    }
}
