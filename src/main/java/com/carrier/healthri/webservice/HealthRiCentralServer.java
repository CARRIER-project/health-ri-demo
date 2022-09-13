package com.carrier.healthri.webservice;

import com.carrier.healthri.webservice.domain.CountIndividualsRequest;
import com.carrier.healthri.webservice.domain.InitCentralServerRequest;
import com.carrier.healthri.webservice.domain.InitDataResponse;
import com.florian.nscalarproduct.station.CentralStation;
import com.florian.nscalarproduct.webservice.CentralServer;
import com.florian.nscalarproduct.webservice.Protocol;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
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
