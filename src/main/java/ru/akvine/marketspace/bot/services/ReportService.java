package ru.akvine.marketspace.bot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;
import ru.akvine.marketspace.bot.repositories.AdvertStatisticRepository;
import ru.akvine.marketspace.bot.utils.POIUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final AdvertStatisticRepository advertStatisticRepository;
    private final ClientService clientService;

    private final static String SHEET_NAME = "sheet";
    private final static int HEADERS_ROW_INDEX = 0;

    public byte[] generateReport(String chatId) {
        logger.info("Generate report for chat id = {}", chatId);

        Long clientId = clientService.verifyExistsByChatId(chatId).getId();
        List<AdvertStatisticEntity> advertStatistics = advertStatisticRepository.findByClientId(clientId);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(SHEET_NAME);
        createHeaders(sheet);

        for (int i = 0; i < advertStatistics.size(); ++i) {
            Row row = sheet.createRow(i + 1);

            Cell idCell = row.createCell(1);
            idCell.setCellValue(advertStatistics.get(i).getId());

            Cell viewsCell = row.createCell(2);
            viewsCell.setCellValue(advertStatistics.get(i).getViews());

            Cell clicksCell = row.createCell(3);
            clicksCell.setCellValue(advertStatistics.get(i).getClicks());

            Cell ctrCell = row.createCell(4);
            ctrCell.setCellValue(advertStatistics.get(i).getCtr());

            Cell cpcCell = row.createCell(5);
            cpcCell.setCellValue(advertStatistics.get(i).getCpc());

            Cell sumCell = row.createCell(6);
            sumCell.setCellValue(advertStatistics.get(i).getSum());

            Cell atbsCell = row.createCell(7);
            atbsCell.setCellValue(advertStatistics.get(i).getAtbs());

            Cell ordersCell = row.createCell(8);
            ordersCell.setCellValue(advertStatistics.get(i).getOrders());

            Cell crCell = row.createCell(9);
            crCell.setCellValue(advertStatistics.get(i).getCr());

            Cell shksCell = row.createCell(10);
            shksCell.setCellValue(advertStatistics.get(i).getShks());

            Cell sumPriceCell = row.createCell(11);
            sumPriceCell.setCellValue(advertStatistics.get(i).getSumPrice());

            Cell advertIdCell = row.createCell(12);
            advertIdCell.setCellValue(advertStatistics.get(i).getAdvertEntity().getAdvertId());

            Cell startDateTimeCell = row.createCell(13);
            startDateTimeCell.setCellValue(advertStatistics.get(i).getAdvertEntity().getStartCheckDateTime().toString());
        }

        return POIUtils.mapToBytes(workbook);
    }

    private void createHeaders(Sheet sheet) {
        Row headersRow = sheet.createRow(HEADERS_ROW_INDEX);

        Cell urlCell = headersRow.createCell(0);
        urlCell.setCellValue("ID");

        Cell viewsCell = headersRow.createCell(1);
        viewsCell.setCellValue("Просмотры");

        Cell clicksCell = headersRow.createCell(2);
        clicksCell.setCellValue("Клики");

        Cell ctrCell = headersRow.createCell(3);
        ctrCell.setCellValue("CTR");

        Cell cpcCell = headersRow.createCell(4);
        cpcCell.setCellValue("CPC");

        Cell sumCell = headersRow.createCell(5);
        sumCell.setCellValue("Затраты");

        Cell atbsCell = headersRow.createCell(6);
        atbsCell.setCellValue("ATBS");

        Cell ordersCell = headersRow.createCell(7);
        ordersCell.setCellValue("ORDERS");

        Cell crCell = headersRow.createCell(8);
        crCell.setCellValue("CR");

        Cell shksCell = headersRow.createCell(9);
        shksCell.setCellValue("SHKS");

        Cell sumPriceCell = headersRow.createCell(10);
        sumPriceCell.setCellValue("SUM_PRICE");

        Cell advertIdCell = headersRow.createCell(11);
        advertIdCell.setCellValue("Advert ID");

        Cell startDateTimeCell = headersRow.createCell(12);
        startDateTimeCell.setCellValue("Время запуска теста");

    }
}
