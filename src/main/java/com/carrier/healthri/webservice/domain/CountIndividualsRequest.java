package com.carrier.healthri.webservice.domain;

import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;

import java.util.List;

public class CountIndividualsRequest {
    private List<AttributeRequirement> requirements;

    public List<AttributeRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<AttributeRequirement> requirements) {
        this.requirements = requirements;
    }

}
