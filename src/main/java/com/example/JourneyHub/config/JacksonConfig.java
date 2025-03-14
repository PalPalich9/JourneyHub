package com.example.JourneyHub.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Сериализатор для Duration
        javaTimeModule.addSerializer(Duration.class, new StdSerializer<Duration>(Duration.class) {
            @Override
            public void serialize(Duration duration, JsonGenerator gen, SerializerProvider provider) throws IOException {
                long totalSeconds = duration.getSeconds();
                long days = totalSeconds / (24 * 3600);
                long hours = (totalSeconds % (24 * 3600)) / 3600;
                long minutes = (totalSeconds % 3600) / 60;
                long seconds = totalSeconds % 60;

                if (days > 0) {
                    gen.writeString(String.format("%d day%s %02d:%02d:%02d", days, days == 1 ? "" : "s", hours, minutes, seconds));
                } else {
                    gen.writeString(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                }
            }
        });

        // Десериализатор для Duration
        javaTimeModule.addDeserializer(Duration.class, new StdDeserializer<Duration>(Duration.class) {
            @Override
            public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getText();
                if (value == null || value.isEmpty()) return null;

                try {
                    String[] parts = value.split(" ");
                    long days = 0;
                    String timePart = value;

                    if (parts.length > 1 && (parts[1].equals("day") || parts[1].equals("days"))) {
                        days = Long.parseLong(parts[0]);
                        timePart = parts[2];
                    }

                    String[] hms = timePart.split(":");
                    long hours = Long.parseLong(hms[0]);
                    long minutes = hms.length > 1 ? Long.parseLong(hms[1]) : 0;
                    long seconds = hms.length > 2 ? Long.parseLong(hms[2]) : 0;

                    return Duration.ofDays(days)
                            .plusHours(hours)
                            .plusMinutes(minutes)
                            .plusSeconds(seconds);
                } catch (Exception e) {
                    throw new IOException("Ошибка при десериализации: " + value, e);
                }
            }
        });

        objectMapper.registerModule(javaTimeModule);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}