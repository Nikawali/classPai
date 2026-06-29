package org.example.classpai.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/** 从 PDF / Word / 纯文本文件提取文字内容 */
public class FileTextExtractor {

    public static String extract(File file) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf")) {
            return extractPdf(file);
        } else if (name.endsWith(".docx")) {
            return extractDocx(file);
        } else if (name.endsWith(".txt")) {
            return new String(java.nio.file.Files.readAllBytes(file.toPath()));
        } else {
            // 不支持的文件类型，返回空
            return "";
        }
    }

    private static String extractPdf(File file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc).trim();
        }
    }

    private static String extractDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText().trim();
        }
    }
}
