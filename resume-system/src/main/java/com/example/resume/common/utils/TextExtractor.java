package com.example.resume.common.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 简历文本提取：支持 PDF / DOCX / TXT
 */
public final class TextExtractor {

    private TextExtractor() {
    }

    public static String extract(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        if (name == null) name = "";
        String lower = name.toLowerCase();
        if (lower.endsWith(".txt")) {
            return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } else if (lower.endsWith(".pdf")) {
            return extractPdf(file.getInputStream());
        } else if (lower.endsWith(".docx")) {
            return extractDocx(file.getInputStream());
        }
        // 其他类型尝试按文本读取
        return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }

    public static String extractPdf(InputStream is) throws IOException {
        try (PDDocument doc = PDDocument.load(is)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    public static String extractDocx(InputStream is) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            return sb.toString();
        }
    }
}
