package ru.akvine.marketspace.bot.unit.utils;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.akvine.marketspace.bot.utils.POIUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("POI utils tests")
public class POIUtilsTest {
    @Test
    @DisplayName("Workbook can't be null")
    public void workbook_cant_be_null() {
        String errorMessage = "Error while map workbook to bytes. Workbook can't be null!";
        assertThatThrownBy(() -> POIUtils.mapToBytes(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("Map workbook to bytes array")
    public void map_workbook_to_bytes_array() {
        Workbook workbook = new XSSFWorkbook();
        byte[] bytes = POIUtils.mapToBytes(workbook);
        assertThat(bytes).isNotEmpty();
    }
}
