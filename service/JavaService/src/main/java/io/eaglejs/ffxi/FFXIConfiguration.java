package io.eaglejs.ffxi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.eaglejs.ffxi.config.CorsConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class FFXIConfiguration extends Configuration {

    @NotEmpty
    private String mongoUri = "mongodb://localhost:27017";

    @Valid
    @NotNull
    private CorsConfiguration cors = new CorsConfiguration();

    @JsonProperty
    public String getMongoUri() {
        return mongoUri;
    }

    @JsonProperty
    public void setMongoUri(String mongoUri) {
        this.mongoUri = mongoUri;
    }

    @JsonProperty
    public CorsConfiguration getCors() {
        return cors;
    }

    @JsonProperty
    public void setCors(CorsConfiguration cors) {
        this.cors = cors;
    }
}
