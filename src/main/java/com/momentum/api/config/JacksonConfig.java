package com.momentum.api.config;

import com.momentum.api.common.response.ApiResponse;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import java.util.List;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(List<JsonMapperBuilderCustomizer> customizers) {
        JsonMapper.Builder builder = JsonMapper.builder();
        customizers.forEach(customizer -> customizer.customize(builder));
        return builder.build();
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
