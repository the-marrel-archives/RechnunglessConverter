package dev.marrel.rechnunglessconverter.metadata;

import org.mustangproject.ZUGFeRD.XRechnungImporter;

public class ProgramVersionExtractor implements MetadataExtractor {
    @Override
    public String getValue(XRechnungImporter xrechung) {
        return "RechnunglessConverter v" + ProgramVersionExtractor.class.getPackage().getImplementationVersion()/* + " w/ MustangProject v" + ZUGFeRDImporter.class.getPackage().getImplementationVersion()*/;
    }
}
