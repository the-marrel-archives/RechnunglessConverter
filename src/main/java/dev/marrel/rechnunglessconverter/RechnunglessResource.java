package dev.marrel.rechnunglessconverter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.marrel.rechnunglessconverter.metadata.METADATAPOINT;
import dev.marrel.rechnunglessconverter.util.FileTools;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mustangproject.ZUGFeRD.XRechnungImporter;
import org.mustangproject.ZUGFeRD.ZUGFeRDVisualizer;
import org.mustangproject.validator.ZUGFeRDValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;


@Path("/")
public class RechnunglessResource {
    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_FAILED = "failed";
    public static final String RESULT_INVALID = "invalid";
    public enum RESULT_KEYS {
        RESULT, MESSAGES, METADATA, ARCHIVE_PDF, ISSUE_DATE
    }

    @GET
    public Response getMain() {
        return Response.ok("RechnunglessConverter - V" + RechnunglessResource.class.getPackage().getImplementationVersion()).build();
    }

    /*
    Return:
    {
        result: success/failed/invalid
        messages:
        issue_date:
        archive_pdf:
        }
     */
    @Path("/convert")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response convert(String xml){
        java.nio.file.Path pdfFile = null;
        String pdfAsBase64;

        ObjectNode resultNode = new ObjectMapper().createObjectNode();

        ValidationResult validationResult = validateInvoiceStream(xml);
        if(validationResult.isValid() || ( System.getenv().containsKey("RECHUNGLESS_PARSEINVALIDXMLS") && System.getenv("RECHUNGLESS_PARSEINVALIDXMLS").equalsIgnoreCase("true") ) ) {
            //XML is valid -> Visualize and extract metadata
            try {
                pdfFile = generatePdfFile(xml);
                pdfAsBase64 = FileTools.fileToBase64String(pdfFile);
            } catch (Exception e) {
                //Internal error during PDF generation -> throw an error back
                e.printStackTrace();
                resultNode.put(RESULT_KEYS.RESULT.name().toLowerCase().toLowerCase(), RESULT_FAILED);
                ArrayNode resultMessagesArray = resultNode.putArray(RESULT_KEYS.MESSAGES.name().toLowerCase());
                resultMessagesArray.add("An internal error occured while trying to generate PDF: " + e.getMessage());
                return Response.serverError().entity(resultNode).build();
            } finally {
                try {
                    Files.delete(pdfFile);
                } catch (IOException ignored) {}
            }

            HashMap<String, String> metadata = getMetadata(xml);

            resultNode.put(RESULT_KEYS.RESULT.name().toLowerCase(), validationResult.isValid() ? RESULT_SUCCESS : RESULT_INVALID);
            /*ObjectNode resultMetadataNode = resultNode.putObject(RESULT_KEYS.METADATA.name().toLowerCase());
            for(String key : metadata.keySet()) {
                resultMetadataNode.put(key, metadata.get(key));
            }*/
            /*ArrayNode resultMetadataArray = resultNode.putArray(RESULT_KEYS.METADATA.name().toLowerCase());
            for(String key : metadata.keySet()) {
                ObjectNode resultMetadataObject = resultMetadataArray.addObject();
                resultMetadataObject.put("namespace", "");
                resultMetadataObject.put("prefix", "");
                resultMetadataObject.put("key", key);
                resultMetadataObject.put("value", metadata.get(key));
            }*/
            if (metadata.containsKey(METADATAPOINT.issueDate.name())) {
                resultNode.put(RESULT_KEYS.ISSUE_DATE.name().toLowerCase(), metadata.get(METADATAPOINT.issueDate.name()));
            }
            resultNode.put(RESULT_KEYS.ARCHIVE_PDF.name().toLowerCase(), pdfAsBase64);
            ArrayNode resultMessagesArray = resultNode.putArray(RESULT_KEYS.MESSAGES.name().toLowerCase());
            for(String s: validationResult.getReasons()){
                resultMessagesArray.add(s);
            }

            System.out.println(resultNode);
            return Response.ok(resultNode).build();
        } else {
            //XML is not valid and the flag to overwrite this check is not set
            resultNode.put(RESULT_KEYS.RESULT.name().toLowerCase(), RESULT_INVALID);
            ArrayNode resultMessagesArray = resultNode.putArray(RESULT_KEYS.MESSAGES.name().toLowerCase());
            for(String message: validationResult.getReasons()) {
                resultMessagesArray.add(message);
            }
            return Response.status(422).entity(resultNode).build();
        }
    }

    @Path("/metadata")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response metadata(String xml) {
        //TODO
        return Response.status(501).build();
    }






    /*
    #########################
    ## Extracting Metadata ##
    #########################
     */

    private HashMap<String, String> getMetadata(String sourceXML) {
        HashMap<String, String> metadataMap = new HashMap<>();
        XRechnungImporter xrech = new XRechnungImporter(new ByteArrayInputStream(sourceXML.getBytes(StandardCharsets.UTF_8)));

        for(METADATAPOINT datapoint : METADATAPOINT.values()) {
            try {
                if (datapoint.getValue(xrech) != null && !datapoint.getValue(xrech).isBlank())
                    metadataMap.put(datapoint.name(), datapoint.getValue(xrech));
                System.out.println(datapoint.name() + " : |" + datapoint.getValue(xrech) + "|");
            } catch (NullPointerException ex) {
                //continue
            }
        }

        return metadataMap;
    }



    /*
    #####################
    ## Generating PDFs ##
    #####################
     */

    private java.nio.file.Path generatePdfFile(String sourceXml) throws IOException {
        java.nio.file.Path tempXmlFile;

        tempXmlFile = Files.createTempFile("rech", ".xml");
        Files.writeString(tempXmlFile, sourceXml,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        java.nio.file.Path pdfFile = generatePdfFile(tempXmlFile);
        Files.delete(tempXmlFile);
        return pdfFile;
    }

    private java.nio.file.Path generatePdfFile(java.nio.file.Path sourceXmlPath) throws IOException {
        java.nio.file.Path pdfFile = Files.createTempFile("rech", ".pdf");

        ZUGFeRDVisualizer zvi = new ZUGFeRDVisualizer();
        zvi.toPDF(sourceXmlPath.toAbsolutePath().toString(), pdfFile.toAbsolutePath().toString());

        System.out.println("Written to " + pdfFile);

        return pdfFile;
    }


    /*
    ##################################
    ## Generating ValidationResults ##
    ##################################
     */


    private ValidationResult validateInvoiceStream(String sourceXml) {
        ZUGFeRDValidator zva = new ZUGFeRDValidator();
        String mustangValidationResult = zva.validate(new ByteArrayInputStream(sourceXml.getBytes(StandardCharsets.UTF_8)), "rechstream.xml");

        return new ValidationResult(mustangValidationResult);
    }


}