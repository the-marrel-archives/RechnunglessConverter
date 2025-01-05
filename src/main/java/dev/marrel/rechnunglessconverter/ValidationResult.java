package dev.marrel.rechnunglessconverter;

import dev.marrel.rechnunglessconverter.util.XMLTools;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;

public class ValidationResult {
    private boolean isValid = false;
    private ArrayList<ValidationMessage> messages = new ArrayList<>();

    public ValidationResult(String mustangValidationXml) {
        Element resultXmlRoot = XMLTools.parseStringXML(mustangValidationXml);

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        try {
            isValid = xpath.evaluate("/validation/summary/@status", resultXmlRoot).equals("valid");
        } catch (XPathExpressionException ex) {
            throw new RuntimeException(ex);
        }

        NodeList messageContainers = resultXmlRoot.getElementsByTagName("messages");
        for(Node messageContainer : XMLTools.nodesAsList(messageContainers)) {
            for(Node messageItem : XMLTools.nodesAsList(messageContainer.getChildNodes())) {
                if ("#text".equals(messageItem.getNodeName())) {
                    continue;
                }
                ValidationMessage validationMessage = new ValidationMessage()
                        .setLevel(messageItem.getNodeName())
                        .setMessage(messageItem.getTextContent().trim());
                if (messageItem instanceof Element messageElement) {
                    validationMessage
                            .setCriterion(messageElement.getAttribute("criterion"))
                            .setLocation(messageElement.getAttribute("location"))
                            .setType(messageElement.getAttribute("type"));
                }
                if (!validationMessage.getMessage().isBlank()) {
                    messages.add(validationMessage);
                }
            }
        }
    }


    public boolean isValid() {
        return isValid;
    }

    public ArrayList<ValidationMessage> getMessages() {
        return messages;
    }
}
