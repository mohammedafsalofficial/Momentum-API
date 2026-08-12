package com.momentum.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.momentum.api.common.response.ApiResponse;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public JsonMapperBuilderCustomizer apiResponseFilterCustomizer() {
        return builder -> {
            SimpleBeanPropertyFilter apiFilter = new SimpleBeanPropertyFilter() {
                @Override
                public void serializeAsProperty(Object pojo, JsonGenerator jgen, SerializationContext provider, PropertyWriter writer) throws Exception {
                    if (pojo instanceof ApiResponse<?>) {
                        ApiResponse<?> response = (ApiResponse<?>) pojo;

                        // If success is true, drop "fieldErrors" from the response payload
                        if (response.isSuccess() && writer.getName().equals("fieldErrors")) {
                            return;
                        }
                        // If success is false, drop "data" from the response payload
                        if (!response.isSuccess() && writer.getName().equals("data")) {
                            return;
                        }
                    }
                    // Keep all other fields exactly as they are
                    super.serializeAsProperty(pojo, jgen, provider, writer);
                }
            };

            // Register the filter with our ApiResponse custom filter ID
            SimpleFilterProvider filterProvider = new SimpleFilterProvider()
                    .addFilter("apiResponseFilter", apiFilter);

            builder.filterProvider(filterProvider);
        };
    }
}
