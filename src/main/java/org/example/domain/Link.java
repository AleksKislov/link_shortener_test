package org.example.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonDeserialize(builder = Link.LinkBuilder.class)
public class Link {
    private String userId;
    private String originalLink;
    private String shortLink;
    private int clicksLimit;
    private long ttl;
    @Builder.Default
    private long createdAt = Instant.now().getEpochSecond();

    @JsonPOJOBuilder(withPrefix = "")
    public static class LinkBuilder {
    }
}
