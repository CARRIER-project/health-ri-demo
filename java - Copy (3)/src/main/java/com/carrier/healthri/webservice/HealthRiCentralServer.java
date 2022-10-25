package com.carrier.healthri.webservice;

import com.carrier.healthri.webservice.domain.CountIndividualsRequest;
import com.carrier.healthri.webservice.domain.InitCentralServerRequest;
import com.carrier.healthri.webservice.domain.InitDataResponse;
import com.carrier.healthri.webservice.domain.Performance;
import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.station.CentralStation;
import com.florian.nscalarproduct.webservice.CentralServer;
import com.florian.nscalarproduct.webservice.Protocol;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class HealthRiCentralServer extends CentralServer {
    private int precision = 0;
    private List<ServerEndpoint> endpoints = new ArrayList<>();
    private ServerEndpoint secretEndpoint;
    private boolean testing;

    public HealthRiCentralServer() {
    }

    public HealthRiCentralServer(boolean testing) {
        this.testing = testing;
    }

    public void initEndpoints(List<ServerEndpoint> endpoints, ServerEndpoint secretServer) {
        //this only exists for testing purposes
        this.endpoints = endpoints;
        this.secretEndpoint = secretServer;
    }

    private void initEndpoints() {
        if (endpoints.size() == 0) {
            endpoints = new ArrayList<>();
            for (String s : servers) {
                endpoints.add(new HealthRiEndpoint(s));
            }
        }
        if (secretEndpoint == null) {
            secretEndpoint = new ServerEndpoint(secretServer);
        }
        endpoints.stream().forEach(x -> x.initEndpoints());
        secretEndpoint.initEndpoints();
    }

    @PostMapping ("initCentralServer")
    public void initCentralServer(@RequestBody InitCentralServerRequest req) {
        //purely exists for vantage6
        super.secretServer = req.getSecretServer();
        super.servers = req.getServers();
    }

    @GetMapping ("performanceTest")
    public Performance performanceTest() {
        AttributeRequirement x1 = new AttributeRequirement();
        x1.setValue(new Attribute(Attribute.AttributeType.numeric, "1", "x1"));
        AttributeRequirement x2 = new AttributeRequirement();
        x2.setValue(new Attribute(Attribute.AttributeType.numeric, "1", "x2"));
        AttributeRequirement x3 = new AttributeRequirement();
        x3.setValue(new Attribute(Attribute.AttributeType.numeric, "1", "x3"));
        AttributeRequirement x4 = new AttributeRequirement();
        x4.setValue(new Attribute(Attribute.AttributeType.numeric, "1", "x4"));
        AttributeRequirement x5 = new AttributeRequirement();
        x5.setValue(new Attribute(Attribute.AttributeType.numeric, "1", "x5"));

        CountIndividualsRequest req1 = new CountIndividualsRequest();
        CountIndividualsRequest req2 = new CountIndividualsRequest();
        CountIndividualsRequest req3 = new CountIndividualsRequest();
        CountIndividualsRequest req4 = new CountIndividualsRequest();
        CountIndividualsRequest req5 = new CountIndividualsRequest();
        req1.setRequirements(Arrays.asList(x1));
        req2.setRequirements(Arrays.asList(x1, x2));
        req3.setRequirements(Arrays.asList(x1, x2, x3));
        req4.setRequirements(Arrays.asList(x1, x2, x3, x4));
        req5.setRequirements(Arrays.asList(x1, x2, x3, x4, x5));

        List<CountIndividualsRequest> reqs = Arrays.asList(req1, req2, req3, req4, req5);

        Performance p = new Performance();
        for (CountIndividualsRequest req : reqs) {
            long time = 0;
            for (int i = 0; i < 100; i++) {
                long start = System.currentTimeMillis();
                sumRelevantValues(req);
                long end = System.currentTimeMillis();
                time += end - start;
            }
            String name = "";
            for (AttributeRequirement r : req.getRequirements()) {
                name += r.getName();
            }
            int averageTime = (int) (time / 100);
            p.getPerformance().put(name, averageTime);
        }
        return p;
    }

    @PostMapping ("sumRelevantValues")
    public int sumRelevantValues(@RequestBody CountIndividualsRequest req) {
        initEndpoints();

        List<ServerEndpoint> relevantEndpoints = new ArrayList<>();
        for (ServerEndpoint endpoint : endpoints) {
            InitDataResponse response = ((HealthRiEndpoint) endpoint).initData(req);
            if (response.isRelevant()) {
                relevantEndpoints.add(endpoint);
            }
        }

        if (relevantEndpoints.size() == 1) {
            // only one relevant party, make things easy and just sum stuff
            return ((HealthRiEndpoint) relevantEndpoints.get(0)).getSum();
        } else {

            secretEndpoint.addSecretStation("start", relevantEndpoints.stream().map(x -> x.getServerId()).collect(
                    Collectors.toList()), relevantEndpoints.get(0).getPopulation());

            BigDecimal result = new BigDecimal(nparty(relevantEndpoints, secretEndpoint).toString());


            return result.intValue();
        }
    }

    private BigInteger nparty(List<ServerEndpoint> endpoints, ServerEndpoint secretEndpoint) {
        CentralStation station = new CentralStation();
        Protocol prot = new Protocol(endpoints, secretEndpoint, "start", precision);
        return station.calculateNPartyScalarProduct(prot);
    }
}
