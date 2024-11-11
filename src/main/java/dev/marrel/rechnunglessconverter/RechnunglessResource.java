package dev.marrel.rechnunglessconverter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.mustangproject.ZUGFeRD.ZUGFeRDVisualizer;
import org.mustangproject.validator.ZUGFeRDValidator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;


@Path("/convert")
public class RechnunglessResource {
    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_FAILED = "failed";
    public static final String RESULT_INVALID = "invalid";

    @GET
    public Response getMain() {
        return Response.ok("RechnunglessConvertert - convert ressource").build();
    }

    @Path("/pdf")
    @POST
    @Produces("application/pdf")
    @Consumes(MediaType.APPLICATION_XML)
    public Response xmltopdf(String xml) throws IOException {

        ValidationResult valResult = validateInvoice(xml);


        java.nio.file.Path pdfFile;
        try {
            pdfFile = performVisualization(xml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Response.ok().entity((StreamingOutput) output -> {
            try {
                Files.copy(pdfFile, output);
            } finally {
                Files.delete(pdfFile);
            }
        }).build();
    }



    /*
    Return:
    {
        result:
        messages:
        archive_text:
        archive_pdf:
     */
    @Path("/pdf2")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_XML)
    public Response xmltopdf2(String xml) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        ValidationResult valResult = validateInvoice(xml);

        java.nio.file.Path pdfFile;
        try {
            pdfFile = performVisualization(xml);
        } catch (IOException e) {
            e.printStackTrace();
            result.put("result", RESULT_FAILED);
            ArrayNode an = result.putArray("messages");
            an.add("Internal error during conversion: " + e.getMessage());
            return Response.serverError().entity(result).build();
        }



        result.put("result", valResult.isValid() ? RESULT_SUCCESS : RESULT_INVALID);
        result.put("archive_pdf", fileToBase64String(pdfFile));
        ArrayNode an = result.putArray("messages");
        for(String s: valResult.getReasons()){
            an.add(s);
        }



        return Response.ok().entity(result).build();
    }

    @Path("/thumbnail")
    @POST
    @Produces("image/jpeg")
    @Consumes(MediaType.APPLICATION_XML)
    public Response xmltothumbnail(String xml) throws IOException {
        java.nio.file.Path pdfFile;
        try {
            pdfFile = performVisualization(xml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Response.ok().entity((StreamingOutput) output -> {
            try {
                PDDocument doc = Loader.loadPDF(pdfFile.toFile());
                PDFRenderer pdfRenderer = new PDFRenderer(doc);
                BufferedImage bffim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
                ImageIO.write(bffim, "jpeg", output);
            } finally {
                Files.delete(pdfFile);
            }
        }).build();
    }


    private static ValidationResult validateInvoice(String sourceXml) throws IOException {
        java.nio.file.Path tempXmlFile, tempPdfFile;

        tempXmlFile = Files.createTempFile("rech", ".xml");
        Files.writeString(tempXmlFile, sourceXml,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);


        ZUGFeRDValidator zva = new ZUGFeRDValidator();
        String res = zva.validate(tempXmlFile.toAbsolutePath().toString());

        return new ValidationResult(res);

    }

    private static java.nio.file.Path performVisualization(String sourceXml) throws IOException {
        java.nio.file.Path tempXmlFile, tempPdfFile;

         tempXmlFile = Files.createTempFile("rech", ".xml");
         Files.writeString(tempXmlFile, sourceXml,
                 StandardCharsets.UTF_8,
                 StandardOpenOption.CREATE,
                 StandardOpenOption.TRUNCATE_EXISTING);
        tempPdfFile = Files.createTempFile("rech", ".pdf");

        //todo validate

        ZUGFeRDVisualizer zvi = new ZUGFeRDVisualizer();

        zvi.toPDF(tempXmlFile.toAbsolutePath().toString(), tempPdfFile.toAbsolutePath().toString());

        System.out.println("Written to " + tempPdfFile);

        Files.delete(tempXmlFile);
        return tempPdfFile;
    }

    private static String fileToBase64String(java.nio.file.Path file) {
        try {
            byte[] fileContent = Files.readAllBytes(file);
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + file, e);
        }
    }
}