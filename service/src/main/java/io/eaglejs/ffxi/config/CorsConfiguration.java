package io.eaglejs.ffxi.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CorsConfiguration {
    
    private boolean enabled = false;
    private String allowedOrigins = "*";
    private String allowedHeaders = "*";
    private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS,HEAD";
    private boolean allowCredentials = true;
    private String exposedHeaders = "Content-Type,Authorization,X-Requested-With";

    @JsonProperty
    public boolean isEnabled() {
        return enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty
    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    @JsonProperty
    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @JsonProperty
    public String getAllowedHeaders() {
        return allowedHeaders;
    }

    @JsonProperty
    public void setAllowedHeaders(String allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    @JsonProperty
    public String getAllowedMethods() {
        return allowedMethods;
    }

    @JsonProperty
    public void setAllowedMethods(String allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    @JsonProperty
    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    @JsonProperty
    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    @JsonProperty
    public String getExposedHeaders() {
        return exposedHeaders;
    }

    @JsonProperty
    public void setExposedHeaders(String exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }
}
