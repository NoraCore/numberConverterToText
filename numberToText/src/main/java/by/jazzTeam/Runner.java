package by.jazzTeam;

import by.jazzTeam.convertServices.NumberConverterBuilder;
import by.jazzTeam.convertServices.NumberToWordsConverter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
public class Runner {
    private static final NumberToWordsConverter converter = (NumberToWordsConverter) NumberConverterBuilder.of();

    public static void main(String[] args) {
        String num = "13456789876543345344567.00034567876540004564000000";

        BigDecimal decimal = new BigDecimal(num);
        String example = converter.convert(decimal);
        log.info("Example of output: {}", example);

        inputNumber();
    }

    public static void inputNumber() {
        boolean flag = true;
        Scanner in = new Scanner(System.in);
        while (flag) {
            log.info("Input a number: ");
            try {
                BigDecimal num = in.nextBigDecimal();
                log.info("Your number:  {}", converter.convert(num));
            } catch (InputMismatchException ex) {
                log.info("Wrong input. Exit");
                flag = false;
            }
        }

        in.close();
    }
}