package dev.marrel.rechnunglessconverter;

import dev.marrel.rechnunglessconverter.metadata.MetadataPoint;
import org.mustangproject.Exceptions.ArithmetricException;
import org.mustangproject.ZUGFeRD.ZUGFeRDExportException;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;
import org.mustangproject.ZUGFeRD.ZUGFeRDVisualizer;
import org.mustangproject.validator.ZUGFeRDValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.HashMap;

public class RechnunglessService {

    /**
     * Extract metadata from the given electronic invoice
     *
     * @param xmlInvoice The XML of an electronic invoice
     * @return The metadata extracted from the electronic invoice
     */
    public HashMap<MetadataPoint, String> getInvoiceMetadata(String xmlInvoice, boolean parseInvalidXmls) throws InvalidInvoiceException {
        try {
            ZUGFeRDImporter invoice = new ZUGFeRDImporter();
            if (parseInvalidXmls) { invoice.doIgnoreCalculationErrors(); }
            invoice.fromXML(xmlInvoice);

            HashMap<MetadataPoint, String> metadataMap = new HashMap<>();
            for(MetadataPoint datapoint : MetadataPoint.values()) {
                if(datapoint.autoProcess) {
                    try {
                        if (datapoint.getValue(invoice) != null && !datapoint.getValue(invoice).isBlank()) {
                            metadataMap.put(datapoint, datapoint.getValue(invoice));
                        }
                    } catch (NullPointerException ex) {
                        //It is possible that NullPointerExceptions occur inside ZUGFeRDImporter if certain values are not present or invalid in the XML.
                        //If this occurs, we ignore the exception and continue as if the value is not present at all.
                        //This does not make the invoice invalid, however.
                    }
                }
            }

            return metadataMap;
        } catch (ArithmeticException | ZUGFeRDExportException e) {
            throw new InvalidInvoiceException("Invalid electronic invoice: " + e.getMessage());
        }
    }

    /**
     * Generate a ZUGFeRD PDF from a XRechnung
     *
     * @param xmlInvoice The XML of an electronic invoice
     * @return The generated pdf file as a base64 String
     * @throws IOException
     */
    public String generateInvoicePdf(String xmlInvoice) throws IOException {

        // Currently there is no exposed API for direct conversion mustangproject, so we use this
        // workaround with temporary files. If we get an improved API, this code should be improved.

        Path tempXmlFile = Files.createTempFile("invoice_", ".xml");
        Path tempPdfFile = Files.createTempFile("invoice_", ".pdf");

        Files.writeString(tempXmlFile, xmlInvoice,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        ZUGFeRDVisualizer zvi = new ZUGFeRDVisualizer();
        zvi.toPDF(tempXmlFile.toAbsolutePath().toString(), tempPdfFile.toAbsolutePath().toString());

        byte[] pdfFile = Files.readAllBytes(tempPdfFile);

        Files.delete(tempXmlFile);
        Files.delete(tempPdfFile);

        return Base64.getEncoder().encodeToString(pdfFile);
    }

    /**
     * Validate the XML of an electronic invoice
     *
     * @param xmlInvoice The electronic invoice to be validated
     * @return A validation result
     */
    protected ValidationResult validateInvoice(String xmlInvoice){
        ZUGFeRDValidator zva = new ZUGFeRDValidator();
        String mustangValidationResult = zva.validate(xmlInvoice.getBytes(StandardCharsets.UTF_8), "invoice-stream.xml");

        String recalculationError = null;
        try {
            new ZUGFeRDImporter().fromXML(xmlInvoice);
        } catch(ZUGFeRDExportException ex) {
            if(ex.getCause() instanceof ArithmetricException) {
                recalculationError = ex.getMessage();
            }
        } catch(Exception ignored) {
        }

        return new ValidationResult(mustangValidationResult, recalculationError);
    }
}
