package org.nmcpye.datarun.template.datatemplateelement.datafield;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ValueType;

import java.io.IOException;

public class FieldDeserializer extends StdDeserializer<AbstractField> {
    public FieldDeserializer() {
        super(AbstractField.class);
    }

    @Override
    public AbstractField deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String type = node.get("type").asText();
        Class<? extends AbstractField> clazz = FormFieldFactory.getPropertyClass(ValueType.valueOf(type));
        // log.info("\n Deserializing properties of type: {}, class {} ", type,
        // clazz.getSimpleName());

        // Deserialize into the appropriate class
        try {
            return jp.getCodec().treeToValue(node, clazz);
        } catch (IllegalArgumentException e) {
            throw new JsonMappingException(jp, "Invalid properties for type: " + type, e);
        }
    }
}
