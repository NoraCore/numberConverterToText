import by.jazzTeam.convertServices.Convertable;
import by.jazzTeam.convertServices.NumberConverterBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberToWordsTest {

    Convertable<Number, String> processor = NumberConverterBuilder.of();

    @Test
    public void testConvertNotFractionNumber() {
        Number testNumber = 1111111111;
        String testWords = "один миллиард сто одиннадцать миллионов сто одиннадцать тысяч сто одиннадцать";
        assertEquals(testWords, processor.convert(testNumber));
    }

    @Test
    public void testZeroInput() {
        Number testNumber = 0.00;
        String testWords = "ноль";
        assertEquals(testWords, processor.convert(testNumber));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void convertNumberFromCSVFile(
            String input, String expected) {
        Number testNum = new BigDecimal(input);
        String actualValue = processor.convert(testNum);
        assertEquals(expected, actualValue);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testVariable.csv", numLinesToSkip = 1)
    void convertNumberFromCSVFileOfSimpleNumeric(
            String input, String expected) {
        Number testNum = new BigDecimal(input);
        String actualValue = processor.convert(testNum);
        assertEquals(expected, actualValue);
    }
}