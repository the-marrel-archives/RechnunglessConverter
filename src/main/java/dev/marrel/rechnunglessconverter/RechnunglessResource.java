package dev.marrel.rechnunglessconverter;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.mustangproject.ZUGFeRD.*;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Path("/convert")
public class RechnunglessResource {
    @GET
    public Response getMain() {
        return Response.ok("HELLO").build();
    }

    @Path("/pdf")
    @POST
    @Produces("application/pdf")
    @Consumes(MediaType.APPLICATION_XML)
    public Response xmltopdf(String xml) throws IOException {
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




    private static java.nio.file.Path performVisualization(String sourceXml) throws IOException {
        java.nio.file.Path tempXmlFile, tempPdfFile;
        try {
             tempXmlFile = Files.createTempFile("rech", ".xml");
             Files.writeString(tempXmlFile, sourceXml,
                     StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING);
            tempPdfFile = Files.createTempFile("rech", ".pdf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //todo validate

        ZUGFeRDVisualizer zvi = new ZUGFeRDVisualizer();
        try {
            zvi.toPDF(tempXmlFile.toAbsolutePath().toString(), tempPdfFile.toAbsolutePath().toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        System.out.println("Written to " + tempPdfFile);

        Files.delete(tempXmlFile);
        return tempPdfFile;
    }
}