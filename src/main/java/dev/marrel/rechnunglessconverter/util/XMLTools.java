package dev.marrel.rechnunglessconverter.util;

import com.helger.commons.io.stream.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class XMLTools {
    public static Element parseStringXML(String data) {
        InputStream is = new StringInputStream(data, StandardCharsets.UTF_8);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        //Build Document
        Document document;
        try {
            document = builder.parse(is);
            is.close();
        } catch (SAXException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Normalize the XML Structure; important !!
        document.getDocumentElement().normalize();

        //Here comes the root node
        return document.getDocumentElement();
    }
}
