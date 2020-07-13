package com.imooc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SupportAddressDTO {
    private Long id;

    @JsonProperty
    private String belongTo;

    @JsonProperty
    private String enName;

    @JsonProperty
    private String cnName;

    @JsonProperty
    private String level;
}
