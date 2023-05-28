package org.lzh.logbook;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;

@Configuration
@ConditionalOnClass(LogbookAutoConfiguration.class)
@PropertySource(value = "classpath:logbook-config.yml", factory = YamlPropertySourceFactory.class)
public class LogbookConfiguration {}
