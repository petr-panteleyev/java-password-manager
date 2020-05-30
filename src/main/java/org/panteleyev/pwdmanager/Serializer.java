/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.pwdmanager;

import org.panteleyev.pwdmanager.model.Card;
import org.panteleyev.pwdmanager.model.Field;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class Serializer {
    // Attributes
    private static final String CLASS_ATTR = "recordClass";
    private static final String UUID_ATTR = "uuid";
    private static final String NAME_ATTR = "name";
    private static final String TYPE_ATTR = "type";
    private static final String MODIFIED_ATTR = "modified";
    private static final String VALUE_ATTR = "value";
    private static final String PICTURE_ATTR = "picture";
    private static final String FAVORITE_ATTR = "favorite";

    // Tags
    private static final String FIELD = "field";
    private static final String FIELDS = FIELD + "s";
    private static final String RECORDS = "records";

    private static final DocumentBuilderFactory DOC_FACTORY;

    static void serialize(OutputStream out, List<Card> records) throws ParserConfigurationException,
        TransformerException
    {
        var docBuilder = DOC_FACTORY.newDocumentBuilder();

        var doc = docBuilder.newDocument();
        var rootElement = doc.createElement("wallet");

        doc.appendChild(rootElement);

        var recordsElement = doc.createElement(RECORDS);
        rootElement.appendChild(recordsElement);

        for (var r : records) {
            var recordXml = serializeRecord(doc, r);
            recordsElement.appendChild(recordXml);
        }

        var transformerFactory = TransformerFactory.newInstance();
        var transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(doc), new StreamResult(out));
    }

    static void deserialize(InputStream in, List<Card> list) throws ParserConfigurationException,
        SAXException, IOException
    {
        var docBuilder = DOC_FACTORY.newDocumentBuilder();

        var doc = docBuilder.parse(in);

        var rootElement = doc.getDocumentElement();
        var records = rootElement.getElementsByTagName("record");
        deserializeRecords(records, list);
    }

    static Element serializeRecord(Document doc, Card r) {
        var xmlRecord = doc.createElement("record");
        xmlRecord.setAttribute(CLASS_ATTR, r.cardClass().name());
        xmlRecord.setAttribute(UUID_ATTR, r.uuid());
        xmlRecord.setAttribute(NAME_ATTR, r.name());
        xmlRecord.setAttribute(TYPE_ATTR, r.type().name());
        xmlRecord.setAttribute(MODIFIED_ATTR, Long.toString(r.modified()));
        xmlRecord.setAttribute(PICTURE_ATTR, r.picture().name());
        xmlRecord.setAttribute(FAVORITE_ATTR, Boolean.toString(r.favorite()));

        // Card - serialize fields
        if (r.isCard()) {
            var fields = r.fields();
            if (!fields.isEmpty()) {
                var fieldsElement = doc.createElement(FIELDS);
                xmlRecord.appendChild(fieldsElement);

                fields.forEach((f) -> {
                    var fe = serializeField(doc, f);
                    fieldsElement.appendChild(fe);
                });
            }

            var note = r.note();
            var noteElement = doc.createElement("note");
            noteElement.setTextContent(note);
            xmlRecord.appendChild(noteElement);
        }

        if (r.isNote()) {
            xmlRecord.appendChild(doc.createTextNode(r.note()));
        }

        return xmlRecord;
    }

    static Element serializeField(Document doc, Field f) {
        var e = doc.createElement(FIELD);
        e.setAttribute(NAME_ATTR, f.name());
        e.setAttribute(TYPE_ATTR, f.type().name());
        e.setAttribute(VALUE_ATTR, f.value());
        return e;
    }

    static Field deserializeField(Element e) {
        var name = e.getAttribute(NAME_ATTR);
        var type = FieldType.valueOf(e.getAttribute(TYPE_ATTR));
        var value = e.getAttribute(VALUE_ATTR);

        return new Field(type, name, value);
    }

    static Card deserializeNote(Element element) {
        var uuid = element.getAttribute(UUID_ATTR);
        if (uuid == null || uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
        }
        var name = element.getAttribute(NAME_ATTR);
        var modified = Long.parseLong(element.getAttribute(MODIFIED_ATTR));
        var text = element.getTextContent();
        var favorite = Boolean.parseBoolean(element.getAttribute(FAVORITE_ATTR));

        return Card.newNote(uuid, modified, name, text, favorite);
    }

    private static void deserializeRecords(NodeList records, List<Card> list) {
        // children
        for (int i = 0; i < records.getLength(); i++) {
            var item = records.item(i);

            if (item instanceof Element element) {
                var recordClass = element.getAttribute(CLASS_ATTR);
                if (recordClass != null) {
                    var record = switch (recordClass) {
                        case "CARD" -> deserializeCard(element);
                        case "NOTE" -> deserializeNote(element);
                        default -> null;
                    };
                    if (record != null) {
                        list.add(record);
                    }
                }
            }
        }
    }

    static Card deserializeCard(Element element) {
        var uuid = element.getAttribute(UUID_ATTR);
        if (uuid == null || uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
        }
        var name = element.getAttribute(NAME_ATTR);
        var picture = Picture.valueOf(element.getAttribute(PICTURE_ATTR));
        var modified = Long.parseLong(element.getAttribute(MODIFIED_ATTR));
        var favorite = Boolean.parseBoolean(element.getAttribute(FAVORITE_ATTR));

        // fields
        var fList = element.getElementsByTagName(FIELD);
        var fields = new ArrayList<Field>(fList.getLength());
        for (int i = 0; i < fList.getLength(); i++) {
            var f = deserializeField((Element) fList.item(i));
            fields.add(f);
        }

        // note
        var note = "";
        var notes = element.getElementsByTagName("note");
        if (notes.getLength() > 0) {
            var noteElement = (Element) notes.item(0);
            note = noteElement.getTextContent();
        }

        return Card.newCard(uuid, modified, name, picture, fields, note, favorite);
    }

    static {
        DOC_FACTORY = DocumentBuilderFactory.newInstance();
    }
}
