package com.carrier.healthri.webservice.domain;

import java.util.HashMap;
import java.util.Map;

public class Performance {
    private Map<String, Integer> performance = new HashMap<>();

    public Map<String, Integer> getPerformance() {
        return performance;
    }

    public void setPerformance(Map<String, Integer> performance) {
        this.performance = performance;
    }
}
