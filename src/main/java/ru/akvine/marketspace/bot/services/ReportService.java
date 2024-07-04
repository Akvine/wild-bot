package ru.akvine.marketspace.bot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.entities.CardPhotoEntity;
import ru.akvine.marketspace.bot.exceptions.CardPhotoNotFoundException;
import ru.akvine.marketspace.bot.repositories.AdvertStatisticRepository;
import ru.akvine.marketspace.bot.repositories.CardPhotoRepository;
import ru.akvine.marketspace.bot.utils.POIUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final AdvertStatisticRepository advertStatisticRepository;
    private final CardService cardService;
    private final CardPhotoRepository cardPhotoRepository;

    private final static String SHEET_NAME = "sheet";
    private final static String MAIN_PHOTO_URL_PATTERN = "1.";
    private final static int HEADERS_ROW_INDEX = 0;

    public byte[] generateReport(String chatId) {
        logger.info("Generate report for chat id = {}", chatId);

        // TODO : N + 1 - достаем по очереди карточки вместо пачки
        List<AdvertStatisticEntity> advertStatistics = advertStatisticRepository.findAll();
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(SHEET_NAME);
        createHeaders(sheet);

        for (int i = 0; i < advertStatistics.size(); ++i) {
            Row row = sheet.createRow(i + 1);

            String itemId = advertStatistics.get(i).getAdvertEntity().getItemId();
            CardEntity cardEntity = cardService.verifyExistsByItemId(itemId);
            List<CardPhotoEntity> cardPhotos = cardPhotoRepository.findByCardId(cardEntity.getId());
            CardPhotoEntity cardPhotoEntity = extractMainPhoto(cardEntity.getUuid(), cardPhotos);

            Cell urlCell = row.createCell(1);
            urlCell.setCellValue(cardPhotoEntity.getBigUrl());

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

        Cell urlCell = headersRow.createCell(1);
        urlCell.setCellValue("URL");

        Cell viewsCell = headersRow.createCell(2);
        viewsCell.setCellValue("Просмотры");

        Cell clicksCell = headersRow.createCell(3);
        clicksCell.setCellValue("Клики");

        Cell ctrCell = headersRow.createCell(4);
        ctrCell.setCellValue("CTR");

        Cell cpcCell = headersRow.createCell(5);
        cpcCell.setCellValue("CPC");

        Cell sumCell = headersRow.createCell(6);
        sumCell.setCellValue("Затраты");

        Cell atbsCell = headersRow.createCell(7);
        atbsCell.setCellValue("ATBS");

        Cell ordersCell = headersRow.createCell(8);
        ordersCell.setCellValue("ORDERS");

        Cell crCell = headersRow.createCell(9);
        crCell.setCellValue("CR");

        Cell shksCell = headersRow.createCell(10);
        shksCell.setCellValue("SHKS");

        Cell sumPriceCell = headersRow.createCell(11);
        sumPriceCell.setCellValue("SUM_PRICE");

        Cell advertIdCell = headersRow.createCell(12);
        advertIdCell.setCellValue("Advert ID");

        Cell startDateTimeCell = headersRow.createCell(13);
        startDateTimeCell.setCellValue("Время запуска теста");

    }

    private CardPhotoEntity extractMainPhoto(String cardUuid, List<CardPhotoEntity> cardsPhotos) {
        return cardsPhotos
                .stream()
                .filter(cardPhoto -> StringUtils.isNotBlank(cardPhoto.getBigUrl()) && cardPhoto.getBigUrl().contains(MAIN_PHOTO_URL_PATTERN))
                .findFirst()
                .orElseThrow(() -> new CardPhotoNotFoundException("Not found main photo for card with uuid = [" + cardUuid + "]"));
    }
}
