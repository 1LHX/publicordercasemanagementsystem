package com.example.publicordercasemanagementsystem.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatCompletionRequest {

    private String model = "qwen-plus";

    private List<ChatMessage> messages = new ArrayList<>();

    private Integer maxTokens;

    private Double temperature;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
}
