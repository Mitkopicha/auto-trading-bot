package com.tradingbot.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BinancePriceService {

  
    private static final String BASE_URL = "https://api.binance.com";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


   
    public BigDecimal getLatestPrice(String symbol) {
        try {
            String url = BASE_URL + "/api/v3/ticker/price?symbol=" + symbol;
            String json = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(json);
            return new BigDecimal(node.get("price").asText());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch latest price from Binance", e);
        }
    }


   

 
    public List<BigDecimal> getHistoricalCloses(String symbol, String interval, int limit) {
        try {
            String url = BASE_URL + "/api/v3/klines?symbol=" + symbol + "&interval=" + interval + "&limit=" + limit;
            String json = restTemplate.getForObject(url, String.class);
            JsonNode arr = objectMapper.readTree(json);
            List<BigDecimal> closes = new ArrayList<>();
            for (JsonNode kline : arr) {
                closes.add(new BigDecimal(kline.get(4).asText())); // close
            }
            return closes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch klines from Binance", e);
        }
    }
}
