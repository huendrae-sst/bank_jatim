package com.bankjatim.jims.controller;

import com.bankjatim.jims.domain.Item;
import com.bankjatim.jims.repository.ItemRepository;
import com.bankjatim.jims.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Exports", description = "Endpoints untuk Unduh Laporan Eksekutif dan Operasional (Excel, PDF, CSV)")
public class ReportController {

    private final ExportService exportService;
    private final ItemRepository itemRepository;

    @GetMapping("/stock-valuation/excel")
    @Operation(summary = "Export Valuasi Persediaan (Excel XLSX)")
    public ResponseEntity<byte[]> exportStockValuationExcel() throws IOException {
        List<Item> items = itemRepository.findAll();
        List<String> headers = Arrays.asList("SKU", "Nama Barang", "Kategori", "Satuan", "Harga Estimasi", "Min Stock", "Safety Stock");
        List<List<Object>> rows = new ArrayList<>();

        for (Item item : items) {
            rows.add(Arrays.asList(
                    item.getSku(),
                    item.getName(),
                    item.getCategory() != null ? item.getCategory().getName() : "-",
                    item.getUom(),
                    item.getEstimatedUnitPrice(),
                    item.getMinStock(),
                    item.getSafetyStock()
            ));
        }

        byte[] excelBytes = exportService.exportToExcel("Valuasi Stok Bank Jatim", headers, rows);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Laporan_Valuasi_Stok_Bank_Jatim.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/stock-valuation/pdf")
    @Operation(summary = "Export Valuasi Persediaan (PDF)")
    public ResponseEntity<byte[]> exportStockValuationPdf() {
        List<Item> items = itemRepository.findAll();
        List<String> headers = Arrays.asList("SKU", "Nama Barang", "Kategori", "Satuan", "Harga", "Min", "Safety");
        List<List<String>> rows = new ArrayList<>();

        for (Item item : items) {
            rows.add(Arrays.asList(
                    item.getSku(),
                    item.getName(),
                    item.getCategory() != null ? item.getCategory().getName() : "-",
                    item.getUom(),
                    "Rp " + item.getEstimatedUnitPrice(),
                    String.valueOf(item.getMinStock()),
                    String.valueOf(item.getSafetyStock())
            ));
        }

        byte[] pdfBytes = exportService.exportToPdf("LAPORAN VALUASI PERSEDIAAN & MASTER BARANG", headers, rows);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Laporan_Valuasi_Stok_Bank_Jatim.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/stock-valuation/csv")
    @Operation(summary = "Export Valuasi Persediaan (CSV)")
    public ResponseEntity<byte[]> exportStockValuationCsv() {
        List<Item> items = itemRepository.findAll();
        List<String> headers = Arrays.asList("SKU", "Nama", "Satuan", "Harga");
        List<List<String>> rows = new ArrayList<>();

        for (Item item : items) {
            rows.add(Arrays.asList(
                    item.getSku(),
                    item.getName().replace(",", " "),
                    item.getUom(),
                    String.valueOf(item.getEstimatedUnitPrice())
            ));
        }

        byte[] csvBytes = exportService.exportToCsv(headers, rows);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Laporan_Valuasi_Stok_Bank_Jatim.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
