package dev.marrel.rechnunglessconverter.metadata;

import org.mustangproject.ZUGFeRD.XRechnungImporter;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;

public enum METADATAPOINT implements MetadataExtractor {
    issueDate ("invoice", ZUGFeRDImporter::getIssueDate),
    dueDate ("invoice", ZUGFeRDImporter::getDueDate),
    totalAmount ("invoice", ZUGFeRDImporter::getAmount),
    buyerName ("invoice", ZUGFeRDImporter::getBuyerTradePartyName),
    paymentTerms ("invoice", ZUGFeRDImporter::getPaymentTerms),
    invoiceID ("invoice", ZUGFeRDImporter::getInvoiceID),
    sellerBIC ("invoice", ZUGFeRDImporter::getBIC),
    sellerBankName ("invoice", ZUGFeRDImporter::getBankName),
    sellerIBAN ("invoice", ZUGFeRDImporter::getIBAN),
    currency ("invoice", ZUGFeRDImporter::getInvoiceCurrencyCode),
    deliveryPeriod ("invoice", new DeliveryPeriodExtractor()),
    programVersion("rechnungless", new ProgramVersionExtractor()),
    //VALIDITY
    ;

    private final MetadataExtractor extractor;
    public final String prefix;
    METADATAPOINT(MetadataExtractor extractor) {
        this("", extractor);
    }
    METADATAPOINT(String prefix, MetadataExtractor extractor) {
        this.extractor = extractor;
        this.prefix = prefix;
    }

    @Override
    public String getValue(XRechnungImporter xrechung) {
        return this.extractor.getValue(xrechung);
    }
}
