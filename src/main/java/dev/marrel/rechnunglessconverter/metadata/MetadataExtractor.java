package dev.marrel.rechnunglessconverter.metadata;

import org.mustangproject.ZUGFeRD.XRechnungImporter;

@FunctionalInterface
public interface MetadataExtractor {
    String getValue(XRechnungImporter xrechung);
}
