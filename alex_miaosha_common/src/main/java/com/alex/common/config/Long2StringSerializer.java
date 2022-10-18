package com.alex.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * description:  修改json，long类型转化成string，解决long返回前端丢失精度的问题
 * author:       majf
 * createDate:   2022/10/14 17:08
 * version:      1.0.0
 */
public class Long2StringSerializer extends JsonSerializer<Long> {

    @Override
    public void serialize(Long value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value != null) {
            jsonGenerator.writeString(value.toString());
        }
    }
}
