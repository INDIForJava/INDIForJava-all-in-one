/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package laazotea.indi;

import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;

/**
 *
 * @author canijillo
 */
public class XMLToString {

  public static String transform(Element xml) {
    try {
      TransformerFactory transFactory = TransformerFactory.newInstance();

      Transformer transformer = transFactory.newTransformer();

      StringWriter buffer = new StringWriter();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.transform(new DOMSource(xml),
              new StreamResult(buffer));
      String str = buffer.toString();
      return str;
    } catch (Exception e) {
    }

    return "";
  }
}