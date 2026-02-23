package com.yieldflow.management.global.external.bithumb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BithumbNoticeDto(
        String[] categories,
        String title,
        @JsonProperty("pc_url") String pcUrl,
        @JsonProperty("published_at") String publishedAt,
        @JsonProperty("modified_at") String modifiedAt

) {

}
