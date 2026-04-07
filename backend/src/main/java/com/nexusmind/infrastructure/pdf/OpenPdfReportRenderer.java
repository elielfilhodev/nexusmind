package com.nexusmind.infrastructure.pdf;

import com.fasterxml.jackson.databind.JsonNode;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

@Component
public class OpenPdfReportRenderer {

    public byte[] render(String title, JsonNode structured) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();
            Color ink = new Color(15, 23, 42);
            Color muted = new Color(51, 65, 85);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, ink);
            Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, ink);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, muted);

            doc.add(new Paragraph(title, titleFont));
            doc.add(new Paragraph(" ", bodyFont));
            appendNode(doc, structured, "", hFont, bodyFont, 0);
            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Falha ao gerar PDF", e);
        }
    }

    private void appendNode(Document doc, JsonNode node, String path, Font hFont, Font bodyFont, int depth)
            throws DocumentException {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String key = e.getKey();
                JsonNode v = e.getValue();
                String label = path.isEmpty() ? key : path + "." + key;
                if (v.isObject() || v.isArray()) {
                    doc.add(new Paragraph(label, hFont));
                    appendNode(doc, v, label, hFont, bodyFont, depth + 1);
                    doc.add(new Paragraph(" ", bodyFont));
                } else {
                    doc.add(new Paragraph(label + ": " + text(v), bodyFont));
                }
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode el = node.get(i);
                if (el.isObject() || el.isArray()) {
                    appendNode(doc, el, path + "[" + i + "]", hFont, bodyFont, depth + 1);
                } else {
                    doc.add(new Paragraph("• " + text(el), bodyFont));
                }
            }
        } else {
            doc.add(new Paragraph(text(node), bodyFont));
        }
    }

    private static String text(JsonNode n) {
        if (n == null || n.isNull()) {
            return "";
        }
        String s = n.asText("");
        if (s.length() > 2000) {
            return s.substring(0, 2000) + "…";
        }
        return new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
