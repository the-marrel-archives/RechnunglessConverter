package dev.marrel.rechnunglessconverter.util;

import com.helger.commons.io.stream.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public class XMLTools {
    private XMLTools() {
    }

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


    //Source: https://stackoverflow.com/a/19591302
    public static List<Node> nodesAsList(NodeList n) {
        return n.getLength() == 0 ?
                Collections.emptyList() : new NodeListWrapper(n);
    }
    static final class NodeListWrapper extends AbstractList<Node>
            implements RandomAccess {
        private final NodeList list;

        NodeListWrapper(NodeList l) {
            list = l;
        }

        public Node get(int index) {
            return list.item(index);
        }

        public int size() {
            return list.getLength();
        }
    }


}
