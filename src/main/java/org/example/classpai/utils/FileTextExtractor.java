package org.example.classpai.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** 从 PDF / Word / 纯文本文件提取文字内容 */
public class FileTextExtractor {

    /** 支持的文本文件扩展名 */
    private static final java.util.Set<String> SUPPORTED = java.util.Set.of(".pdf", ".docx", ".txt");

    /** 判断文件类型是否支持文字提取 */
    public static boolean isSupported(String fileName) {
        if (fileName == null) return false;
        String name = fileName.toLowerCase();
        for (String ext : SUPPORTED) {
            if (name.endsWith(ext)) return true;
        }
        return false;
    }

    /** 提取文件中的纯文本 */
    public static String extract(File file) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf")) {
            return extractPdf(file);
        } else if (name.endsWith(".docx")) {
            return extractDocx(file);
        } else if (name.endsWith(".txt")) {
            return extractTxt(file);
        } else {
            return "";
        }
    }

    private static String extractPdf(File file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);  // 按位置排序，保持阅读顺序
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

    /** 提取纯文本，自动检测编码（UTF-8 → GBK → 系统默认） */
    private static String extractTxt(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        // 尝试常见编码
        for (Charset cs : new Charset[]{
                StandardCharsets.UTF_8,
                Charset.forName("GBK"),
                Charset.defaultCharset()
        }) {
            try {
                String text = new String(bytes, cs);
                // 简单启发式：如果包含常见中文字符，大概率编码正确
                if (containsCommonChars(text)) {
                    return text.trim();
                }
            } catch (Exception ignored) {}
        }
        // 兜底用 UTF-8
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

    /** 检查文本中是否包含常见字符（汉字、英文、数字等），用于判断编码正确性 */
    private static boolean containsCommonChars(String text) {
        if (text == null || text.isEmpty()) return false;
        for (int i = 0; i < Math.min(text.length(), 200); i++) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
                return true;
            }
        }
        return false;
    }
}
