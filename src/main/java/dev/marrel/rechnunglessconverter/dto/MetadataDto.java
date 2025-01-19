package dev.marrel.rechnunglessconverter.dto;

public class MetadataDto {

    private String namespace = "";
    private String prefix = "";
    private String key;
    private String value;

    public String getNamespace() {
        return namespace;
    }

    public MetadataDto setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public MetadataDto setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getKey() {
        return key;
    }

    public MetadataDto setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public MetadataDto setValue(String value) {
        this.value = value;
        return this;
    }
}
