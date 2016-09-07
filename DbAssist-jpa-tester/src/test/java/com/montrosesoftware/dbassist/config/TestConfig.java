package com.montrosesoftware.dbassist.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("com.montrosesoftware.entities, com.montrosesoftware.repositories")
@PropertySource("classpath:/config/application.properties")
public class TestConfig {}
