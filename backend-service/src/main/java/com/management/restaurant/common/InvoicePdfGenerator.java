package com.management.restaurant.common;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.management.restaurant.model.PreOrder;
import org.springframework.stereotype.Component;


import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Component
public class InvoicePdfGenerator {
    public byte[] generateInvoice(String fullName, String tableName, String bookingTime,
                                  int guests, List<PreOrder> preOrders, BigDecimal totalAmount) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font bodyFont = new Font(Font.HELVETICA, 12);

            Paragraph title = new Paragraph("HÓA ĐƠN ĐẶT BÀN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            doc.add(title);

            doc.add(new Paragraph("Khách hàng: " + fullName, bodyFont));
            doc.add(new Paragraph("Bàn: " + tableName, bodyFont));
            doc.add(new Paragraph("Thời gian: " + bookingTime, bodyFont));
            doc.add(new Paragraph("Số khách: " + guests, bodyFont));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 1, 2, 2});
            table.addCell("Tên món");
            table.addCell("SL");
            table.addCell("Đơn giá");
            table.addCell("Thành tiền");

            for (PreOrder pre : preOrders) {
                if (pre.getDish() == null) continue;
                table.addCell(pre.getDish().getName());
                table.addCell(String.valueOf(pre.getQuantity()));
                table.addCell(pre.getDish().getPrice().toString());

                BigDecimal total = pre.getDish().getPrice()
                        .multiply(BigDecimal.valueOf(pre.getQuantity()));
                table.addCell(total.toString());
            }

            doc.add(table);
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Tổng cộng: " + totalAmount.toPlainString() + " VND", bodyFont));

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo PDF: " + e.getMessage());
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }
}
