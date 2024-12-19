package dev.marrel.rechnunglessconverter;

import dev.marrel.rechnunglessconverter.util.XMLTools;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;

public class ValidationResult {
    private boolean isValid;
    private ArrayList<String> reasons;

    public ValidationResult(String mustangValidationXml) {
        reasons = new ArrayList<>();

        Element e = XMLTools.parseStringXML(mustangValidationXml);

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        try {
            isValid = xpath.evaluate("/validation/summary/@status", e).equals("valid");
        } catch (XPathExpressionException ex) {
            throw new RuntimeException(ex);
        }

        if(!isValid) {
            NodeList messageNodes = e.getElementsByTagName("messages");
            for(int i=0; i< messageNodes.getLength(); i++) {
                Element messageElement = (Element) messageNodes.item(i);
                NodeList errorNodes = messageElement.getElementsByTagName("error");
                for(int k=0; k<errorNodes.getLength(); k++) {
                    Element errorElement = (Element) errorNodes.item(k);
                    StringBuilder sb = new StringBuilder();
                    if(errorElement.hasAttribute("type")) sb.append("type=\"").append(errorElement.getAttribute("type")).append("\"; ");
                    if(errorElement.hasAttribute("location")) sb.append("location=\"").append(errorElement.getAttribute("location")).append("\"; ");
                    if(errorElement.hasAttribute("criterion")) sb.append("criterion=\"").append(errorElement.getAttribute("criterion")).append("\"; ");
                    sb.append(errorElement.getTextContent());
                    reasons.add(sb.toString());
                }
            }

        }
    }


    public boolean isValid() {
        return isValid;
    }

    public ArrayList<String> getReasons() {
        return reasons;
    }
}
