package com.defender.config;

public class ConfigProvider {
    public static String getKafkaBootstrapServers() {
        return System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");//TO BE CHANGE TO kafka:29092
    }

    public static String getMongoHost() {
        return System.getenv().getOrDefault("MONGODB_HOST", "localhost"); //TO BE CHANGED TO mongodb
    }

    public static int getMongoPort() {
        return Integer.parseInt(System.getenv().getOrDefault("MONGODB_PORT", "27017"));
    }

    public static String getMongoDatabase() {
        return System.getenv().getOrDefault("MONGODB_DATABASE", "sms_db");
    }
}