import java.io.FileReader;
import java.io.IOException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class AMFPatcher {
	public static boolean isVolumeMatching(String name, Element volume) {
		NodeList md = volume.getElementsByTagName("metadata");
		for (int i=0; i < md.getLength(); i++) {
			Element el = (Element)md.item(i);
			if ("name".equals(el.getAttribute("type")) && el.getFirstChild().getNodeValue().indexOf(name) > -1) return true;
		}
		return false;
	}
	
	public static Element getVolume(String name, Document doc) {
		NodeList volumes = doc.getElementsByTagName("volume");
		for (int i=0; i < volumes.getLength(); i++) {
			if (isVolumeMatching(name, (Element)volumes.item(i))) return (Element)volumes.item(i);
		}
		return null;
	}
	
	public static void stripMetadataFromVolume(Element volume) {
		Node next = volume.getFirstChild();
		while (next != null) {
			if ("metadata".equals(next.getNodeName()) && !"name".equals(((Element)next).getAttribute("type"))) {
				Node xx = next;
				next = next.getNextSibling();
				volume.removeChild(xx);
			} else {
				next = next.getNextSibling();
			}
		}
	}

	
	public static void processVolume(JsonObject jobj, Document doc) throws Exception {
		System.out.println("Processing object '" + jobj.getString("name") + "'");
		Element vol = getVolume(jobj.getString("name"), doc);
		if (vol == null) throw new Exception("Volume not found");
		System.out.println("Stripping existing metadata...");
		stripMetadataFromVolume(vol);
		for (String key : jobj.keySet()) {
			if (!key.startsWith("slic3r.")) continue;
			System.out.println("adding metadata: " + key + " = " + jobj.getString(key));
			Element md = doc.createElement("metadata");
			md.setAttribute("type", key);
			md.appendChild(doc.createTextNode(jobj.getString(key)));
			vol.appendChild(md);
		}
		
	}
	public static void main(String[] args) throws Exception {
		
		String fname = args[0];
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(fname);
//		System.out.println("find vladi: " + getVolume("vladi", doc));
//		System.out.println("find CO2Box OLED 5 walls: " + getVolume("CO2Box OLED 5 walls", doc));
//		stripMetadataFromVolume(getVolume("CO2Box OLED 5 win gra", doc));
		
		JsonReader jr = Json.createReader(new FileReader(args[1]));
		JsonStructure jsonst = jr.read();
		if (jsonst instanceof JsonArray) {
			for (JsonValue el : (JsonArray)jsonst) {
				JsonObject obj = (JsonObject)el;
				processVolume(obj, doc);
				//System.out.println("json :" + obj.getString("name"));
				
			}
			
		}
		
		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.transform(new DOMSource(doc), new StreamResult( "c:/Users/i024148/Mobile Docs/My Documents/3d print/CO2 Case v1/cccc.xml"));
		//NodeList volumes = doc.getElementsByTagName("volume");
		
		// TODO Auto-generated method stub

	}

}
