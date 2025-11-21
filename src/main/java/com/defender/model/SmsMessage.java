package com.defender.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SmsMessage {
    @JsonProperty("sender")
    private String sender;
    
    @JsonProperty("receiver")
    private String receiver;
    
    @JsonProperty("message")
    private String message;
}