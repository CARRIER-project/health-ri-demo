package com.carrier.healthri.webservice;

import com.carrier.healthri.webservice.domain.CountIndividualsRequest;
import com.carrier.healthri.webservice.domain.InitCentralServerRequest;
import com.carrier.healthri.webservice.domain.Performance;
import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import org.junit.jupiter.api.Test;

import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthRiEndpointTest {

    public HealthRiEndpointTest() throws NoSuchPaddingException, NoSuchAlgorithmException {
    }

    @Test
    public void testValuesMultiplicationX2_PredictorAndOutComeLocal()
            throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException {
        HealthRiServer serverZ = new HealthRiServer("resources/performance/data1_1000.csv", "Z");
        HealthRiEndpoint endpointZ = new HealthRiEndpoint(serverZ);

        HealthRiServer server3 = new HealthRiServer("resources/performance/data3_1000.csv", "3");
        HealthRiEndpoint endpoint3 = new HealthRiEndpoint(server3);

        HealthRiServer server4 = new HealthRiServer("resources/performance/data4_1000.csv", "4");
        HealthRiEndpoint endpoint4 = new HealthRiEndpoint(server4);

        HealthRiServer server5 = new HealthRiServer("resources/performance/data5_1000.csv", "5");
        HealthRiEndpoint endpoint5 = new HealthRiEndpoint(server5);

        HealthRiServer server2 = new HealthRiServer("resources/performance/data2_1000.csv", "2");
        HealthRiEndpoint endpoint2 = new HealthRiEndpoint(server2);


        HealthRiServer secret = new HealthRiServer("6", Arrays.asList(endpointZ, endpoint2, endpoint3, endpoint4,
                                                                      endpoint5));
        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpointZ);
        all.add(endpoint2);
        all.add(endpoint3);
        all.add(endpoint4);
        all.add(endpoint5);
        all.add(secretEnd);
        secret.setEndpoints(all);
        serverZ.setEndpoints(all);
        server2.setEndpoints(all);
        server3.setEndpoints(all);
        server4.setEndpoints(all);
        server5.setEndpoints(all);

        HealthRiCentralServer central = new HealthRiCentralServer(true);
        central.initEndpoints(Arrays.asList(endpointZ, endpoint2), secretEnd);

        central.initEndpoints(Arrays.asList(endpointZ, endpoint2), secretEnd);

        Performance p = central.performanceTest();
        System.out.println(p);
    }

    @Test
    public void testCountOneRequirement()
            throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException {
        HealthRiServer serverZ = new HealthRiServer("resources/smallK2Example_secondhalf.csv", "Z");
        HealthRiEndpoint endpointZ = new HealthRiEndpoint(serverZ);

        HealthRiServer server2 = new HealthRiServer("resources/smallK2Example_firsthalf.csv", "2");
        HealthRiEndpoint endpoint2 = new HealthRiEndpoint(server2);

        AttributeRequirement req = new AttributeRequirement();
        req.setValue(new Attribute(Attribute.AttributeType.numeric, "1", "x1"));
        //this selects individuals: 1,2,4,7, & 9


        HealthRiServer secret = new HealthRiServer("3", Arrays.asList(endpointZ, endpoint2));
        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpointZ);
        all.add(endpoint2);
        all.add(secretEnd);
        secret.setEndpoints(all);
        serverZ.setEndpoints(all);
        server2.setEndpoints(all);

        HealthRiCentralServer central = new HealthRiCentralServer(true);
        central.initEndpoints(Arrays.asList(endpointZ, endpoint2), secretEnd);

        central.initEndpoints(Arrays.asList(endpointZ, endpoint2), secretEnd);

        CountIndividualsRequest request = new CountIndividualsRequest();
        request.setRequirements(Arrays.asList(req));
        double result = central.sumRelevantValues(request);
        int expected = 5;

        assertEquals(result, expected);
    }

    @Test
    public void testCountTwoRequirement()
            throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException {
        HealthRiServer serverZ = new HealthRiServer("resources/smallK2Example_secondhalf.csv", "Z");
        HealthRiEndpoint endpointZ = new HealthRiEndpoint(serverZ);

        HealthRiServer server2 = new HealthRiServer("resources/smallK2Example_firsthalf.csv", "2");
        HealthRiEndpoint endpoint2 = new HealthRiEndpoint(server2);

        AttributeRequirement req = new AttributeRequirement();
        req.setValue(new Attribute(Attribute.AttributeType.numeric, "1", "x1"));
        AttributeRequirement req2 = new AttributeRequirement();
        req2.setValue(new Attribute(Attribute.AttributeType.numeric, "1", "x2"));
        //this selects individuals: 2,4,7, & 9


        HealthRiServer secret = new HealthRiServer("3", Arrays.asList(endpointZ, endpoint2));
        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpointZ);
        all.add(endpoint2);
        all.add(secretEnd);
        secret.setEndpoints(all);
        serverZ.setEndpoints(all);
        server2.setEndpoints(all);

        HealthRiCentralServer central = new HealthRiCentralServer(true);
        central.initEndpoints(Arrays.asList(endpointZ, endpoint2), secretEnd);

        central.initEndpoints(Arrays.asList(endpointZ, endpoint2), secretEnd);

        CountIndividualsRequest request = new CountIndividualsRequest();
        request.setRequirements(Arrays.asList(req, req2));
        double result = central.sumRelevantValues(request);
        int expected = 4;

        assertEquals(result, expected);
    }

    @Test
    public void testCountRequirementRange()
            throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException {
        HealthRiServer serverZ = new HealthRiServer("resources/smallK2Example_secondhalf.csv", "Z");
        HealthRiEndpoint endpointZ = new HealthRiEndpoint(serverZ);

        HealthRiServer server2 = new HealthRiServer("resources/smallK2Example_firsthalf.csv", "2");
        HealthRiEndpoint endpoint2 = new HealthRiEndpoint(server2);


        Attribute lower = new Attribute(Attribute.AttributeType.numeric, "0", "x1");
        Attribute higher = new Attribute(Attribute.AttributeType.numeric, "1", "x1");
        AttributeRequirement req = new AttributeRequirement(lower, higher);
        //this selects individuals:1, 2,4,7, & 9


        HealthRiServer secret = new HealthRiServer("3", Arrays.asList(endpointZ, endpoint2));
        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpointZ);
        all.add(endpoint2);
        all.add(secretEnd);
        secret.setEndpoints(all);
        serverZ.setEndpoints(all);
        server2.setEndpoints(all);

        HealthRiCentralServer central = new HealthRiCentralServer(true);
        central.initEndpoints(Arrays.asList(endpointZ, endpoint2), secretEnd);

        central.initEndpoints(Arrays.asList(endpointZ, endpoint2), secretEnd);

        CountIndividualsRequest request = new CountIndividualsRequest();
        request.setRequirements(Arrays.asList(req));
        double result = central.sumRelevantValues(request);
        int expected = 5;

        assertEquals(result, expected);
    }

    @Test
    public void testCountHybridSplit()
            throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException {
        HealthRiServer serverZ = new HealthRiServer("resources/smallK2Example_secondhalf.csv", "Z");
        HealthRiEndpoint endpointZ = new HealthRiEndpoint(serverZ);

        HealthRiServer server2 = new HealthRiServer("resources/smallK2Example_firsthalf.csv", "2");
        HealthRiEndpoint endpoint2 = new HealthRiEndpoint(server2);

        AttributeRequirement req = new AttributeRequirement();
        req.setValue(new Attribute(Attribute.AttributeType.numeric, "2", "x4"));
        AttributeRequirement req2 = new AttributeRequirement();
        req2.setValue(new Attribute(Attribute.AttributeType.numeric, "1", "x1"));
        //this selects individuals: 1,2,4,7, & 9


        HealthRiServer secret = new HealthRiServer("3", Arrays.asList(endpointZ, endpoint2));
        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpointZ);
        all.add(endpoint2);
        all.add(secretEnd);
        secret.setEndpoints(all);
        serverZ.setEndpoints(all);
        server2.setEndpoints(all);

        HealthRiCentralServer central = new HealthRiCentralServer(true);
        central.initEndpoints(Arrays.asList(endpointZ, endpoint2), secretEnd);

        central.initEndpoints(Arrays.asList(endpointZ, endpoint2), secretEnd);

        CountIndividualsRequest request = new CountIndividualsRequest();
        request.setRequirements(Arrays.asList(req, req2));
        double result = central.sumRelevantValues(request);
        int expected = 5;

        assertEquals(result, expected);
    }

    @Test
    public void testinitCentralServerRequest() {

        HealthRiCentralServer central = new HealthRiCentralServer(true);
        InitCentralServerRequest req = new InitCentralServerRequest();
        req.setSecretServer("secret");
        req.setServers(Arrays.asList("1", "2"));
        central.initCentralServer(req);
    }
}