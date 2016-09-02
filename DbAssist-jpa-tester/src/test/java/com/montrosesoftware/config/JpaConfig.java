package com.montrosesoftware.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan("com.montrosesoftware")
@Configuration
@EnableJpaRepositories(basePackages = { "com.montrosesoftware.repositories" })
@EnableAutoConfiguration
public class JpaConfig {}
