package dev.marrel.rechnunglessconverter.metadata;

import org.mustangproject.ZUGFeRD.Version;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;

public class ProgramVersionExtractor implements MetadataExtractor {
    @Override
    public String getValue(ZUGFeRDImporter invoice) {
        return "RechnunglessConverter v" + ProgramVersionExtractor.class.getPackage().getImplementationVersion() + " w/ MustangProject v" + Version.VERSION;
    }
}
