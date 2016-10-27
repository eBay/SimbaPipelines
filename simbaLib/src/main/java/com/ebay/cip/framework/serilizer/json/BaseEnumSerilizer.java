package com.ebay.cip.framework.serilizer.json;

import com.ebay.cip.framework.util.CipClassUtil;
import com.ebay.kernel.BaseEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.IntNode;

import java.io.IOException;

/**
 * Created by jagmehta on 6/17/2015.
 */
public class BaseEnumSerilizer {

    public static class BaseEnumJsonSerializer extends JsonSerializer<BaseEnum> {

        @Override
        public void serialize(BaseEnum object, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            if (object instanceof BaseEnum) {
                BaseEnum o = (BaseEnum)object;
                jsonGenerator.writeStartObject();
                jsonGenerator.writeNumberField("id", o.getId());
                jsonGenerator.writeStringField("Value",object.getName());
                jsonGenerator.writeEndObject();
            }
        }
    }


    public static class BaseEnumJsonDeSerializer extends JsonDeserializer<BaseEnum> {

        @Override
        public BaseEnum deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            //deserializationContext.
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            int id = (Integer) ((IntNode) node.get("id")).numberValue();
            try {
                Class claz = CipClassUtil.getClass(node.get("Value").asText());
                return BaseEnum.getEnum(claz,id);
            } catch (ClassNotFoundException e) {
                //TODO cal log
            }
            return null;
        }
    }

}
