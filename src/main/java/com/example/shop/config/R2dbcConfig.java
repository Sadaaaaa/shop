package com.example.shop.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class R2dbcConfig extends AbstractR2dbcConfiguration {
    @Value("${spring.r2dbc.url}")
    private String url;

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                        .from(ConnectionFactoryOptions.parse(url))
                        .option(ConnectionFactoryOptions.USER, username)
                        .option(ConnectionFactoryOptions.PASSWORD, password)
                        .build()
        );
    }

    @Override
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Object> converters = new ArrayList<>();
        converters.add(new ByteBufferReadingConverter());
        converters.add(new ByteBufferWritingConverter());
        return new R2dbcCustomConversions(getStoreConversions(), converters);
    }

    @WritingConverter
    private static class ByteBufferWritingConverter implements Converter<ByteBuffer, ByteBuffer> {
        @Override
        public ByteBuffer convert(ByteBuffer source) {
            return source;
        }
    }

    @ReadingConverter
    private static class ByteBufferReadingConverter implements Converter<Object, ByteBuffer> {
        @Override
        public ByteBuffer convert(Object source) {
            if (source instanceof ByteBuffer) {
                return (ByteBuffer) source;
            }
            return null;
        }
    }
}
