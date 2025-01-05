package dev.marrel.rechnunglessconverter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.marrel.rechnunglessconverter.metadata.MetadataPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetadataResponseDto {

    private String result;

    private List<MetadataDto> metadata;


    public String getResult() {
        return result;
    }

    public MetadataResponseDto setResult(String result) {
        this.result = result;
        return this;
    }

    public List<MetadataDto> getMetadata() {
        return metadata;
    }

    public MetadataResponseDto setMetadata(Map<MetadataPoint, String> metadata) {
        this.metadata = new ArrayList<>();
        for(MetadataPoint metadataPoint : metadata.keySet()) {
            this.metadata.add(
                    new MetadataDto()
                        .setPrefix(metadataPoint.prefix)
                        .setKey(metadataPoint.name())
                        .setValue(metadata.get(metadataPoint))
            );
        }
        return this;
    }
}
