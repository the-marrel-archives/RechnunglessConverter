package dev.marrel.rechnunglessconverter.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import dev.marrel.rechnunglessconverter.ValidationMessage;

import java.util.List;


public class ConversionResponseDto {

    private String result;

    private List<ValidationMessage> messages;

    private String metadata;

    @JsonProperty("archive_pdf")
    private String archivePdf;

    @JsonProperty("issue_date")
    private String issueDate;


    public String getResult() {
        return result;
    }

    public ConversionResponseDto setResult(String result) {
        this.result = result;
        return this;
    }

    public List<ValidationMessage> getMessages() {
        return messages;
    }

    public ConversionResponseDto setMessages(List<ValidationMessage> messages) {
        this.messages = messages;
        return this;
    }

    public String getMetadata() {
        return metadata;
    }

    public ConversionResponseDto setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getArchivePdf() {
        return archivePdf;
    }

    public ConversionResponseDto setArchivePdf(String archivePdf) {
        this.archivePdf = archivePdf;
        return this;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public ConversionResponseDto setIssueDate(String issueDate) {
        this.issueDate = issueDate;
        return this;
    }
}