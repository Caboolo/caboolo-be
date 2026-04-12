package com.caboolo.backend.hub.util;

import com.caboolo.backend.hub.dto.HubDto;
import com.caboolo.backend.hub.enums.City;
import com.caboolo.backend.hub.enums.HubType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelParserUtil {

    public static List<HubDto> parseHubs(InputStream is) {
        List<HubDto> hubs = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header row
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();
                HubDto hub = new HubDto();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIdx) {
                        case 0: // Name
                            hub.setName(currentCell.getStringCellValue());
                            break;
                        case 1: // Type
                            hub.setType(HubType.valueOf(currentCell.getStringCellValue().trim().toUpperCase()));
                            break;
                        case 2: // City
                            hub.setCity(City.valueOf(currentCell.getStringCellValue().trim().toUpperCase()));
                            break;
                        case 3: // Priority
                            hub.setPriority((int) currentCell.getNumericCellValue());
                            break;
                        case 4: // Latitude
                            hub.setLatitude(currentCell.getNumericCellValue());
                            break;
                        case 5: // Longitude
                            hub.setLongitude(currentCell.getNumericCellValue());
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }

                hubs.add(hub);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return hubs;
    }
}
