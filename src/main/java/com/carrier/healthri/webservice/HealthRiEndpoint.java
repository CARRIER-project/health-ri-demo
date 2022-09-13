package com.carrier.healthri.webservice;

import com.carrier.healthri.webservice.domain.CountIndividualsRequest;
import com.carrier.healthri.webservice.domain.InitDataResponse;
import com.florian.nscalarproduct.webservice.Server;
import com.florian.nscalarproduct.webservice.ServerEndpoint;

public class HealthRiEndpoint extends ServerEndpoint {
    public HealthRiEndpoint(Server server) {
        super(server);
    }

    public HealthRiEndpoint(String url) {
        super(url);
    }

    public int getSum() {
        if (testing) {
            return ((HealthRiServer) (server)).getSum();
        } else {
            return REST_TEMPLATE.getForEntity(serverUrl + "/getSum", Integer.class).getBody();
        }
    }

    public InitDataResponse initData(CountIndividualsRequest req) {
        if (testing) {
            return ((HealthRiServer) (server)).initData(req);
        } else {
            return REST_TEMPLATE.postForEntity(serverUrl + "/initData", req, InitDataResponse.class).getBody();
        }
    }

}
