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
        logger.info("Generate report");

        Long clientId = clientService.verifyExistsByChatId(chatId).getId();
        List<AdvertStatisticEntity> advertStatistics = advertStatisticRepository.findByClientId(clientId);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(SHEET_NAME);
        createHeaders(sheet);

        for (int i = 0; i < advertStatistics.size(); ++i) {
            Row row = sheet.createRow(i + 1);

            Cell idCell = row.createCell(0);
            idCell.setCellValue(advertStatistics.get(i).getId());

            Cell viewsCell = row.createCell(1);
            viewsCell.setCellValue(advertStatistics.get(i).getViews());

            Cell clicksCell = row.createCell(2);
            clicksCell.setCellValue(advertStatistics.get(i).getClicks());

            Cell ctrCell = row.createCell(3);
            ctrCell.setCellValue(advertStatistics.get(i).getCtr());

            Cell cpcCell = row.createCell(4);
            cpcCell.setCellValue(advertStatistics.get(i).getCpc());

            Cell sumCell = row.createCell(5);
            sumCell.setCellValue(advertStatistics.get(i).getSum());

            Cell atbsCell = row.createCell(6);
            atbsCell.setCellValue(advertStatistics.get(i).getAtbs());

            Cell ordersCell = row.createCell(7);
            ordersCell.setCellValue(advertStatistics.get(i).getOrders());

            Cell crCell = row.createCell(8);
            crCell.setCellValue(advertStatistics.get(i).getCr());

            Cell shksCell = row.createCell(9);
            shksCell.setCellValue(advertStatistics.get(i).getShks());

            Cell sumPriceCell = row.createCell(10);
            sumPriceCell.setCellValue(advertStatistics.get(i).getSumPrice());

            Cell advertIdCell = row.createCell(11);
            advertIdCell.setCellValue(advertStatistics.get(i).getAdvertEntity().getExternalId());

            Cell startDateTimeCell = row.createCell(12);
            startDateTimeCell.setCellValue(advertStatistics.get(i).getAdvertEntity().getStartCheckDateTime().toString());
        }

        return POIUtils.mapToBytes(workbook);
    }

    private void createHeaders(Sheet sheet) {
        Row headersRow = sheet.createRow(HEADERS_ROW_INDEX);

        Cell urlCell = headersRow.createCell(0);
        urlCell.setCellValue("ID статистики");

        Cell viewsCell = headersRow.createCell(1);
        viewsCell.setCellValue("Просмотры (views)");

        Cell clicksCell = headersRow.createCell(2);
        clicksCell.setCellValue("Клики (clicks)");

        Cell ctrCell = headersRow.createCell(3);
        ctrCell.setCellValue("Показатель кликабельности (ctr)");

        Cell cpcCell = headersRow.createCell(4);
        cpcCell.setCellValue("Средняя стоимость клика (cpc)");

        Cell sumCell = headersRow.createCell(5);
        sumCell.setCellValue("Затраты");

        Cell atbsCell = headersRow.createCell(6);
        atbsCell.setCellValue("Количество добавлений товаров в корзину (atbs)");

        Cell ordersCell = headersRow.createCell(7);
        ordersCell.setCellValue("Количество заказов (orders)");

        Cell crCell = headersRow.createCell(8);
        crCell.setCellValue("Отношение количества заказов к общему количеству посещений кампании (cr)");

        Cell shksCell = headersRow.createCell(9);
        shksCell.setCellValue("Количество заказанных товаров (shks)");

        Cell sumPriceCell = headersRow.createCell(10);
        sumPriceCell.setCellValue("Заказов на сумму (sum_price)");

        Cell advertIdCell = headersRow.createCell(11);
        advertIdCell.setCellValue("Идентификатор рекламной кампании (Advert ID)");

        Cell startDateTimeCell = headersRow.createCell(12);
        startDateTimeCell.setCellValue("Время запуска теста");

    }
}
