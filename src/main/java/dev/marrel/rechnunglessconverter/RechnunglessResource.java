package dev.marrel.rechnunglessconverter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.marrel.rechnunglessconverter.dto.ConversionResponseDto;
import dev.marrel.rechnunglessconverter.dto.MetadataResponseDto;
import dev.marrel.rechnunglessconverter.metadata.MetadataPoint;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


@Path("/")
public class RechnunglessResource {

    private static final RechnunglessService RECHNUNGLESS = new RechnunglessService();

    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_FAILED = "failed";
    public static final String RESULT_INVALID = "invalid";

    @GET
    public Response getMain() {
        return Response.ok("RechnunglessConverter - V" + RechnunglessResource.class.getPackage().getImplementationVersion()).build();
    }

    @Path("/version")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVersion() {
        String programVersion = RechnunglessResource.class.getPackage().getImplementationVersion();
        String[] programVersionSplit = programVersion.split("\\.");

        ObjectNode resultNode = new ObjectMapper().createObjectNode();
        resultNode.put("major", Integer.parseInt(programVersionSplit[0]));
        resultNode.put("minor", Integer.parseInt(programVersionSplit[1]));
        resultNode.put("patch", Integer.parseInt(programVersionSplit[2]));

        return Response.ok(resultNode).build();
    }

    /**
     * Visualize an electronic invoice as a PDF
     * @param xmlInvoice The electronic invoice to be visualized
     * @return A {@link ConversionResponseDto} with the generated PDF and some metadata
     */
    @Path("/convert")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response convert(String xmlInvoice){
        List<ValidationMessage> validationMessages = new ArrayList<>();
        try {
            final ValidationResult validationResult = RECHNUNGLESS.validateInvoice(xmlInvoice);
            validationMessages = validationResult.getMessages();
            //TODO: remove exception, just use validationResult
            final String invoicePdfBase64 = RECHNUNGLESS.generateInvoicePdf(xmlInvoice);

            final HashMap<MetadataPoint, String> metadata = RECHNUNGLESS.getInvoiceMetadata(xmlInvoice);

            final ConversionResponseDto responseDto = new ConversionResponseDto()
                    .setResult(validationResult.isValid() ? RESULT_SUCCESS : RESULT_INVALID)
                    .setArchivePdf(invoicePdfBase64)
                    .setIssueDate(metadata.getOrDefault(MetadataPoint.issueDate, null))
                    .setMessages(validationResult.getMessages());

            return Response.ok(responseDto).build();

        } catch (InvalidInvoiceException e) {
            validationMessages.addAll(e.getValidationMessages());
            final ConversionResponseDto responseDto = new ConversionResponseDto()
                    .setResult(RESULT_INVALID)
                    .setMessages(validationMessages);
            return Response.status(422).entity(responseDto).build();

        } catch (Exception e) {
            // Unknown internal error during PDF generation -> throw an error back
            System.err.println(e.getMessage());
            final ConversionResponseDto responseDto = new ConversionResponseDto()
                    .setResult(RESULT_FAILED)
                    .setMessages(Collections.singletonList(new ValidationMessage().setMessage(
                            "An internal error occurred while trying to generate PDF: " + e.getMessage()
                    )));
            return Response.serverError().entity(responseDto).build();
        }
    }

    /**
     * Extract the full metadata of an electronic invoice
     * @param xmlInvoice The electronic invoice of which the metadata shall be extracted
     * @return A {@link MetadataResponseDto} with the full metadata of the electronic invoice
     */
    //TODO
    @Path("/metadata")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response metadata(String xmlInvoice) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        try {
            final ValidationResult validationResult = RECHNUNGLESS.validateInvoice(xmlInvoice);
            validationMessages = validationResult.getMessages();
            //TODO: remove exception, just use validationResult

            final HashMap<MetadataPoint, String> metadata = RECHNUNGLESS.getInvoiceMetadata(xmlInvoice);

            final MetadataResponseDto responseDto = new MetadataResponseDto()
                    .setResult(validationResult.isValid() ? RESULT_SUCCESS : RESULT_INVALID)
                    .setMetadata(metadata);
                    //.setIssueDate(metadata.getOrDefault(MetadataPoint.issueDate, null))
                    //.setMessages(validationResult.getMessages());

            return Response.ok(responseDto).build();

        } catch (InvalidInvoiceException e) {
            //validationMessages.addAll(e.getValidationMessages());
            final MetadataResponseDto responseDto = new MetadataResponseDto()
                    .setResult(RESULT_INVALID);
                    //.setMessages(validationMessages);
            return Response.status(422).entity(responseDto).build();

        } catch (Exception e) {
            // Unknown internal error during PDF generation -> throw an error back
            System.err.println(e.getMessage());
            final MetadataResponseDto responseDto = new MetadataResponseDto()
                    .setResult(RESULT_FAILED);
                    /*.setMessages(Collections.singletonList(new ValidationMessage().setMessage(
                            "An internal error occurred while trying to generate PDF: " + e.getMessage()
                    )));*/
            return Response.serverError().entity(responseDto).build();
        }
    }
}