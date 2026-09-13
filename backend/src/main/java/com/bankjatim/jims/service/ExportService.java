package com.bankjatim.jims.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    public byte[] exportToExcel(String sheetName, List<String> headers, List<List<Object>> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Header styling
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex()); // Bank Jatim Maroon
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerCellStyle);
            }

            // Create Data Rows
            int rowIdx = 1;
            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(rowIdx++);
                for (int colIdx = 0; colIdx < rowData.size(); colIdx++) {
                    Object val = rowData.get(colIdx);
                    Cell cell = row.createCell(colIdx);
                    if (val instanceof Number) {
                        cell.setCellValue(((Number) val).doubleValue());
                    } else if (val != null) {
                        cell.setCellValue(val.toString());
                    } else {
                        cell.setCellValue("-");
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportToPdf(String title, List<String> headers, List<List<String>> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph titlePara = new Paragraph("PT BANK PEMBANGUNAN DAERAH JAWA TIMUR Tbk\n" + title + "\n\n", titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            document.add(titlePara);

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            Font headFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            for (String header : headers) {
                com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new Paragraph(header, headFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
            for (List<String> row : rows) {
                for (String col : row) {
                    com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new Paragraph(col != null ? col : "-", bodyFont));
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Gagal menggenerate PDF laporan: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    public byte[] exportToCsv(List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append("\n");
        for (List<String> row : rows) {
            sb.append(String.join(",", row)).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
