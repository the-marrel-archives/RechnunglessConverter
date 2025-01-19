package dev.marrel.rechnunglessconverter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.marrel.rechnunglessconverter.dto.ConversionResponseDto;
import dev.marrel.rechnunglessconverter.dto.MetadataResponseDto;
import dev.marrel.rechnunglessconverter.metadata.MetadataPoint;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
        resultNode.put("major",programVersionSplit[0]);
        resultNode.put("minor", programVersionSplit[1]);
        resultNode.put("patch", programVersionSplit[2]);

        return Response.ok(resultNode).build();
    }

    /**
     * Visualize an electronic invoice as a PDF
     * @param xmlInvoice The electronic invoice to be visualized
     * @param parseInvalidXmlsParam A "True" or "False" string to determine if we should try to process the xml, even when it's invalid
     * @return A {@link ConversionResponseDto} with the generated PDF and some metadata
     */
    @Path("/convert")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response convert(String xmlInvoice, @DefaultValue("false") @QueryParam("parseinvalidxmls") String parseInvalidXmlsParam){
        boolean parseInvalidXmls = parseInvalidXmlsParam.equalsIgnoreCase("true");
        ValidationResult validationResult = RECHNUNGLESS.validateInvoice(xmlInvoice);
        List<ValidationMessage> validationMessages = validationResult.getMessages();
        if(validationResult.isValid() || parseInvalidXmls) {
            try {
                final String invoicePdfBase64 = RECHNUNGLESS.generateInvoicePdf(xmlInvoice);

                final HashMap<MetadataPoint, String> metadata = RECHNUNGLESS.getInvoiceMetadata(xmlInvoice, parseInvalidXmls);

                final ConversionResponseDto responseDto = new ConversionResponseDto()
                        .setResult(validationResult.isValid() ? RESULT_SUCCESS : RESULT_INVALID)
                        .setArchivePdf(invoicePdfBase64)
                        .setIssueDate(metadata.getOrDefault(MetadataPoint.issueDate, null))
                        .setMessages(validationResult.getMessages());

                return Response.ok(responseDto).build();

            } catch (InvalidInvoiceException e) {
                //Exception occurred during metadata extraction -> Use default process down below
                //This catch should be obsolete, since recalculation errors are now caught during validation
                validationMessages.addAll(e.getValidationMessages());

            } catch (Exception e) {
                // Unknown internal error during PDF generation/metadata extraction - either program failure or while parsing an invalid xml -> return an error
                e.printStackTrace();
                final ConversionResponseDto responseDto = new ConversionResponseDto()
                        .setResult(RESULT_FAILED)
                        .setMessages(Collections.singletonList(new ValidationMessage().setMessage(
                                "An error occurred while trying to generate PDF. Maybe this file isn't and invoice? " + e.getMessage()
                        )));
                return Response.serverError().entity(responseDto).build();
            }
        }

        //Either invoice was determined to be invalid during validation (and the flag is not set) or during metadata extraction - in any case we treat it as an invalid invoice
        final ConversionResponseDto responseDto = new ConversionResponseDto()
                .setResult(RESULT_INVALID)
                .setMessages(validationMessages);
        return Response.status(422).entity(responseDto).build();
    }

    /**
     * Extract the metadata of an electronic invoice
     * @param xmlInvoice The electronic invoice of which the metadata shall be extracted
     * @param parseInvalidXmlsParam A "True" or "False" string to determine if we should try to process the xml, even when it's invalid
     * @return A {@link MetadataResponseDto} with the metadata of the electronic invoice
     */
    @Path("/metadata")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response metadata(String xmlInvoice, @DefaultValue("false") @QueryParam("parseinvalidxmls") String parseInvalidXmlsParam){
        boolean parseInvalidXmls = parseInvalidXmlsParam.equalsIgnoreCase("true");
        ValidationResult validationResult = RECHNUNGLESS.validateInvoice(xmlInvoice);
        List<ValidationMessage> validationMessages = validationResult.getMessages();
        if(validationResult.isValid() || parseInvalidXmls) {
            try {
                final HashMap<MetadataPoint, String> metadata = RECHNUNGLESS.getInvoiceMetadata(xmlInvoice, parseInvalidXmls);
                metadata.put(MetadataPoint.validity, validationResult.isValid() ? "VALID" : "INVALID");

                final MetadataResponseDto responseDto = new MetadataResponseDto()
                        .setResult(validationResult.isValid() ? RESULT_SUCCESS : RESULT_INVALID)
                        .setMetadata(metadata)
                        .setMessages(validationResult.getMessages());

                return Response.ok(responseDto).build();

            } catch (InvalidInvoiceException e) {
                //Exception occurred during metadata extraction -> Use default process down below
                //This catch should be obsolete, since recalculation errors are now caught during validation
                validationMessages.addAll(e.getValidationMessages());

            } catch (Exception e) {
                // Unknown internal error during metadata extraction - either program failure or while parsing an invalid xml -> return an error
                System.err.println(e.getMessage());
                final ConversionResponseDto responseDto = new ConversionResponseDto()
                        .setResult(RESULT_FAILED)
                        .setMessages(Collections.singletonList(new ValidationMessage().setMessage(
                                "An internal error occurred while trying to extract metadata: " + e.getMessage()
                        )));
                return Response.serverError().entity(responseDto).build();
            }
        }

        //Either invoice was determined to be invalid during validation (and the flag is not set) or during metadata extraction - in any case we treat it as an invalid invoice
        final MetadataResponseDto responseDto = new MetadataResponseDto()
            .setResult(RESULT_INVALID)
            .setMessages(validationMessages);
        return Response.status(422).entity(responseDto).build();

    }
}