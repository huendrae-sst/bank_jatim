import { toast } from '@/utils/toast';

/**
 * Utility untuk ekspor data tabel menjadi file CSV yang diunduh langsung di browser
 * Mendukung UTF-8 BOM agar terbaca sempurna di Microsoft Excel
 */
export function exportToCsv(filename, headers, rows) {
  if (!rows || !rows.length) {
    toast.warn('Tidak ada data untuk diekspor.');
    return;
  }

  const escapeCsv = (str) => {
    if (str === null || str === undefined) return '""';
    const s = String(str).replace(/"/g, '""');
    return `"${s}"`;
  };

  const headerLine = headers.map((h) => escapeCsv(h.label || h.title || h)).join(',');
  const rowLines = rows.map((row) => {
    if (Array.isArray(row)) {
      return row.map(escapeCsv).join(',');
    }
    return headers
      .map((h) => {
        const key = h.key || h.field || h;
        return escapeCsv(row[key]);
      })
      .join(',');
  });

  const csvContent = '\uFEFF' + [headerLine, ...rowLines].join('\r\n');
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', filename.endsWith('.csv') ? filename : `${filename}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}
