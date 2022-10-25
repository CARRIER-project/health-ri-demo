package com.carrier.healthri.webservice;

import com.carrier.healthri.webservice.domain.Performance;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class test {

    @Test
    public void testPerformance() {
        RestTemplate REST_TEMPLATE = new RestTemplate();
        Performance average = new Performance();
        for (int i = 0; i < 5; i++) {
            Performance p = REST_TEMPLATE.getForEntity("http://localhost:8080/performanceTest", Performance.class)
                    .getBody();
            for (String k : p.getPerformance().keySet()) {
                if (average.getPerformance().keySet().contains(k)) {
                    average.getPerformance().put(k, p.getPerformance().get(k) + average.getPerformance().get(k));
                } else {
                    average.getPerformance().put(k, p.getPerformance().get(k));
                }
            }
        }
        for (String k : average.getPerformance().keySet()) {
            average.getPerformance().put(k, average.getPerformance().get(k) / 100);
        }
        
        for (String k : average.getPerformance().keySet()) {
            System.out.println(k + " " + average.getPerformance().get(k) + "ms");
        }
    }
}
