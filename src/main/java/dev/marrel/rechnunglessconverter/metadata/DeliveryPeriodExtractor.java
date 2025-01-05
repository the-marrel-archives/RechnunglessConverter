package dev.marrel.rechnunglessconverter.metadata;

import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;

import java.text.SimpleDateFormat;

public class DeliveryPeriodExtractor implements MetadataExtractor{
    @Override
    public String getValue(ZUGFeRDImporter invoice) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
        String result = sdf.format(invoice.getDetailedDeliveryPeriodFrom());
        if(invoice.getDetailedDeliveryPeriodTo() != null)
            result += " - " + sdf.format(invoice.getDetailedDeliveryPeriodTo());
        return result;
    }
}
