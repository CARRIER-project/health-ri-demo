package com.carrier.healthri.webservice;

import com.carrier.healthri.webservice.domain.CountIndividualsRequest;
import com.carrier.healthri.webservice.domain.InitDataResponse;
import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.data.Data;
import com.florian.nscalarproduct.station.DataStation;
import com.florian.nscalarproduct.webservice.Server;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

import static com.florian.nscalarproduct.data.Parser.parseCsv;

@RestController
public class HealthRiServer extends Server {
    private static final int DEFAULT_PRECISION = 0; //checkstyle's a bitch
    private int precision = DEFAULT_PRECISION; //precision for the n-party protocol since that works with integers

    private Data data;

    @Value ("${datapath}")
    private String path;

    public HealthRiServer() throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException {
        super();
        this.initEndpoints();
    }

    public HealthRiServer(String id)
            throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException {
        this.serverId = id;
    }


    public HealthRiServer(String id, List<ServerEndpoint> endpoints)
            throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException {
        this.serverId = id;
        this.setEndpoints(endpoints);
    }

    public HealthRiServer(String path, String id)
            throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException {
        this.path = path;
        this.serverId = id;
        readData();
    }

    @GetMapping ("getSum")
    public int getSum() {
        BigInteger sum = BigInteger.ZERO;
        for (BigInteger d : this.localData) {
            sum = sum.add(d);
        }
        return sum.intValue();
    }

    @PostMapping ("initData")
    public InitDataResponse initData(@RequestBody CountIndividualsRequest request) {
        reset();
        if (this.data == null) {
            readData();
        }

        boolean requirementPresent = isRequirementPresent(request.getRequirements());

        if (requirementPresent) {
            // If the time variable is locally present select individuals
            selectIndividuals(request.getRequirements());
        }
        if (requirementPresent) {
            this.population = localData.length;
            this.dataStations.put("start", new DataStation(this.serverId, this.localData));
        }
        InitDataResponse response = new InitDataResponse();
        response.setRequirementPresent(requirementPresent);
        return response;
    }

    private boolean isRequirementPresent(
            List<AttributeRequirement> requirements) {
        boolean requirementPresent = false;
        for (AttributeRequirement r : requirements) {
            if (isLocallyPresent(r.getName())) {
                // there can theoretically be multiple requirements, only care if at least 1 is locally present
                requirementPresent = true;
                break;
            }
        }
        return requirementPresent;
    }

    private boolean isLocallyPresent(String predictor) {
        return this.data.getAttributeCollumn(predictor) != null;
    }

    private void selectIndividuals(List<AttributeRequirement> reqs) {
        //method to select appropriate individuals.
        //Assumption is that they're onl selected based on eventtime
        //But it is possible to select on multiple attributes at once
        reset();
        if (this.data == null) {
            readData();
        }
        localData = new BigInteger[population];
        for (int i = 0; i < population; i++) {
            localData[i] = BigInteger.ONE;
        }


        List<List<Attribute>> values = data.getData();
        for (AttributeRequirement req : reqs) {
            for (int i = 0; i < population; i++) {
                if (isLocallyPresent(req.getName())) {
                    Attribute a = values.get(data.getAttributeCollumn(req.getName())).get(i);
                    if (locallyUnknown(a)) {
                        // attribute is locally unknown so ignore it in this vector, another party will correct this
                        continue;
                    } else if (!req.checkRequirement(a)) {
                        // attribute is locally known and the check fails
                        localData[i] = BigInteger.ZERO;
                    }
                }
            }
        }

        checkHorizontalSplit(data, localData);

        this.population = localData.length;
        this.dataStations.put("start", new DataStation(this.serverId, this.localData));
    }

    private boolean locallyUnknown(Attribute a) {
        return a.isUnknown();
    }

    private void readData() {
        if (System.getenv("DATABASE_URI") != null) {
            // Check if running in vantage6 by looking for system env, if yes change to database_uri system env for path
            this.path = System.getenv("DATABASE_URI");
        }
        this.data = parseCsv(path, 0);
        this.population = data.getNumberOfIndividuals();
    }

    @Override
    protected void reset() {
        dataStations = new HashMap<>();
        secretStations = new HashMap<>();
    }
}
