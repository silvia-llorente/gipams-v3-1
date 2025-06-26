package gcs.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class MetadataUtil {
    public String[] parseDG(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document d = builder.parse(is);
        String dg_id, title, type, abs, projC, desc;
        Element root = d.getDocumentElement();
        if (root.getElementsByTagName("Title").getLength() != 0) {
            Node n = root.getElementsByTagName("Title").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetGroupMetadata")) {
                title = n.getTextContent();
            } else title = null;
        } else title = null;
        if (root.getElementsByTagName("UUID").getLength() != 0) {
            Node n = root.getElementsByTagName("UUID").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetGroupMetadata")) {
                dg_id = n.getTextContent();
            } else dg_id = "-1";
        } else dg_id = "-1";
        if (root.getElementsByTagName("Type").getLength() != 0) {
            Node n = root.getElementsByTagName("Type").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetGroupMetadata")) {
                n = n.getAttributes().getNamedItem("existing_study_type");
                if (n != null) {
                    type = n.getNodeValue();
                }else type = null;
            } else type = null;
        } else type = null;
        if (root.getElementsByTagName("Abstract").getLength() != 0) {
            Node n = root.getElementsByTagName("Abstract").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetGroupMetadata")) {
                abs = n.getTextContent();
            } else abs = null;
        } else abs = null;
        if (root.getElementsByTagName("ProjectCentreName").getLength() != 0) {
            Node n = root.getElementsByTagName("ProjectCentreName").item(0);
            if (n.getParentNode().getNodeName().equals("ProjectCentre")) {
                projC = n.getTextContent();
            } else projC = null;
        }  else projC = null;
        if (root.getElementsByTagName("Description").getLength() != 0) {
            Node n = root.getElementsByTagName("Description").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetGroupMetadata")) {
                desc = n.getTextContent();
            } else desc = null;
        } else desc = null;
        //db.Modifiers.insertDG(dg_id,title,type,abs,projC,desc,dg.getOwner(),dg.getMpegfile().getId());
        String[] dg = new String[]{dg_id,title,type,abs,projC,desc};
        return dg;
    }
        
    public String[] parseDT(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document d = builder.parse(is);
        String dt_id, title, type, abs, projC, desc;
        Element root = d.getDocumentElement();
        if (root.getElementsByTagName("Title").getLength() != 0) {
            Node n = root.getElementsByTagName("Title").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetMetadata")) {
                title = n.getTextContent();
            } else title = null;
        } else title = null;
        if (root.getElementsByTagName("UUID").getLength() != 0) {
            Node n = root.getElementsByTagName("UUID").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetMetadata")) {
                dt_id = n.getTextContent();
            } else dt_id = "-1";
        } else dt_id = "-1";
        if (root.getElementsByTagName("Type").getLength() != 0) {
            Node n = root.getElementsByTagName("Type").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetMetadata")) {
                n = n.getAttributes().getNamedItem("existing_study_type");
                if (n != null) {
                    type = n.getNodeValue();
                }else type = null;
            } else type = null;
        } else type = null;
        if (root.getElementsByTagName("Abstract").getLength() != 0) {
            Node n = root.getElementsByTagName("Abstract").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetMetadata")) {
                abs = n.getTextContent();
            } else abs = null;
        } else abs = null;
        if (root.getElementsByTagName("ProjectCentreName").getLength() != 0) {
            Node n = root.getElementsByTagName("ProjectCentreName").item(0);
            if (n.getParentNode().getNodeName().equals("ProjectCentre")) {
                projC = n.getTextContent();
            } else projC = null;
        }  else projC = null;
        if (root.getElementsByTagName("Description").getLength() != 0) {
            Node n = root.getElementsByTagName("Description").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetMetadata")) {
                desc = n.getTextContent();
            } else desc = null;
        } else desc = null;
        String[] dg = new String[]{dt_id,title,type,abs,projC,desc};
        return dg;
    }
}