package dev.marrel.rechnunglessconverter.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.marrel.rechnunglessconverter.ValidationMessage;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversionResponseDto {

    private String result;

    private List<ValidationMessage> messages;

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