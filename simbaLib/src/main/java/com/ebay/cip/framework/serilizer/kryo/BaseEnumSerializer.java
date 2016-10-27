package com.ebay.cip.framework.serilizer.kryo;

import com.ebay.kernel.BaseEnum;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

/**
 * Created by jagmehta on 6/17/2015.
 */
public class BaseEnumSerializer {
    public static class BaseEnumKryoSerializer extends FieldSerializer {
        public BaseEnumKryoSerializer(Kryo kryo, Class type) {
            super(kryo, type);
        }

        public BaseEnum read(Kryo kryo, Input input, Class type) {
            int id = input.readInt(false);
            return BaseEnum.getEnum(type,id);
        }
    }
}
