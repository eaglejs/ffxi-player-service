package io.eaglejs.ffxi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotEmpty;

public class FFXIConfiguration extends Configuration {

    @NotEmpty
    private String mongoUri = "mongodb://localhost:27017";

    @JsonProperty
    public String getMongoUri() {
        return mongoUri;
    }

    @JsonProperty
    public void setMongoUri(String mongoUri) {
        this.mongoUri = mongoUri;
    }
}
