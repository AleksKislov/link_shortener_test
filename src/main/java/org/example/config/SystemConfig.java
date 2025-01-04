package org.example.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonDeserialize(builder = SystemConfig.SystemConfigBuilder.class)
public class SystemConfig {
    private String linkBaseUrl;
    private int linkTTL;
    private int clicksMinimum;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SystemConfigBuilder {
    }
}
