package com.example.make_decision_helper.config.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.example.make_decision_helper.repository")
@EnableMongoAuditing
public class MongoConfig {
} 