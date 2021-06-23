package by.jazzTeam.convertServices;

import by.jazzTeam.csvWorkers.CsvParser;
import by.jazzTeam.model.ScaleRecord;
import by.jazzTeam.model.VariableRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class NumberToWordsConverter implements Convertable<Number, String> {

    private static final String MINUS = "минус";
    private static final String ZERO = "ноль";
    private static final String POINT = "целых";
    private static final String POINT_WITH_CHANGED_ENDS = "целая";
    private final String SEPARATOR = "[.]";
    private final CsvParser<ScaleRecord> unitParser;
    private final CsvParser<VariableRecord> variableParser;
    private final CsvParser<VariableRecord> tensParser;
    private final CsvParser<VariableRecord> hundredsParser;
    private final CsvParser<ScaleRecord> unitFactorParser;

    public NumberToWordsConverter(CsvParser<ScaleRecord> unitParser,
                                  CsvParser<VariableRecord> variableParser,
                                  CsvParser<VariableRecord> tensParser,
                                  CsvParser<VariableRecord> hundredsParser,
                                  CsvParser<ScaleRecord> unitFactorParser) {
        this.unitParser = unitParser;
        this.variableParser = variableParser;
        this.tensParser = tensParser;
        this.hundredsParser = hundredsParser;
        this.unitFactorParser = unitFactorParser;
    }

    public String convert(Number numberToConvert) {
        log.debug("_______Start_____");
        StringBuilder stringBuilder = new StringBuilder();
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        boolean isAFractionalNumber = isAFractionalNumber(numberToConvert);

        CompletableFuture<String> wholePart =
                CompletableFuture.supplyAsync(
                        () -> convertWholePart(getWholePart(numberToConvert), isAFractionalNumber),
                        executor);

        CompletableFuture<String> fractionalPart =isAFractionalNumber?
                CompletableFuture.supplyAsync(
                        () -> convertFractionalPart(getFractionalPart(numberToConvert)),executor): CompletableFuture.completedFuture("");
        try {
            stringBuilder.append(wholePart.get()).append(fractionalPart.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }finally {
            shutdownAndAwaitTermination(executor);
        }
        log.debug("_______end_____");
        return stringBuilder.toString().trim();
    }


    private String convertWholePart(String wholePart, boolean isFractionalNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isANegativeNumber(wholePart)) {

            stringBuilder.append(MINUS).append(" ");
            wholePart = wholePart.substring(1);
        }
        if (wholePart.length() == 1 && Integer.parseInt(wholePart.substring(wholePart.length() - 1)) == 0) {
            stringBuilder.append(ZERO).append(" ");

        } else {
            convertDigitsInWordsRecursive(wholePart, 0, isFractionalNumber, stringBuilder);
        }
        if (isFractionalNumber) {
            addEndingOfWholePartNumeric(wholePart, stringBuilder);
        }
        return stringBuilder.toString();
    }

    private String convertFractionalPart(String fractionalPart) {
        StringBuilder stringBuilder = new StringBuilder();
        String fractNum = getNumberWithoutLastZeros(fractionalPart);

        if (fractNum.isBlank()) return "";

        convertDigitsInWordsRecursive(fractNum, 0, true, stringBuilder);

        String fractionalScaleEnds = getUnitsWordsOfFractionPart(fractNum);
        appendIfNotBlank(fractionalScaleEnds, stringBuilder);
        return stringBuilder.toString();
    }

    private String getNumberWithoutLastZeros(String fractionalPart) {
        StringBuilder fractionalPartReversed = new StringBuilder(fractionalPart).reverse();
        StringBuilder accumulator = new StringBuilder();

        fractionalPartReversed.toString().chars()
                .mapToObj(i -> (char) i)
                .dropWhile(ch -> ch.equals('0')).forEach(accumulator::append);

        accumulator.reverse();
        return accumulator.toString();
    }


    private void convertDigitsInWordsRecursive(String numSub, int unit, boolean isFractionalNumber, StringBuilder stringBuilder) {
        boolean lastCycle = numSub.length() <= 3;
        int temp = lastCycle ? Integer.parseInt(numSub) :
                Integer.parseInt(numSub.substring(numSub.length() - 3));
        int hundredPlaces = temp / 100;
        int tensPlaces = temp - hundredPlaces * 100;

        if (!lastCycle) {
            convertDigitsInWordsRecursive(numSub.substring(0, numSub.length() - 3),
                    unit + 1,
                    isFractionalNumber, stringBuilder);
        }
        if (temp == 0) {
            return;
        }


        String hundreds = convertHundreds(hundredPlaces);
        boolean flag = changeOneOrTwo(unit, tensPlaces, isFractionalNumber);
        String tens = isLessThanTwenty(tensPlaces) ?
                getSimpleNumeral(tensPlaces, flag) :
                convertTens(tensPlaces, flag).trim();

        String unitText = unitParser.parse(unit).textFormat();

        log.debug("3 last number --> [{}],  words of hundreds  --> [{}] words of tens --> [{}], Unit --> [{}]",
                temp, hundreds, tens, unitText);
        appendIfNotBlank(hundreds, stringBuilder);
        appendIfNotBlank(tens, stringBuilder);

        ScaleRecord unitRecord = unitParser.parse(unit);
        StringBuilder stringScaleBuilder = new StringBuilder(unitRecord.textFormat());
        changeEndsOfScale(tensPlaces, unitRecord.scale(), stringScaleBuilder);
        appendIfNotBlank(stringScaleBuilder.toString(), stringBuilder);
    }

    private boolean changeOneOrTwo(int unit, int twoLastSigns, boolean isFractional) {
        int lastDigit = twoLastSigns - twoLastSigns / 10 * 10;
        boolean lastDigitIsOneOrTwoOtZero = lastDigit <= 2;
        boolean exceptSimpleNumeric = (!isLessThanTwenty(twoLastSigns) || twoLastSigns < 3);
        boolean isZero = lastDigit == 0;
        boolean f = (unit == 0 && isFractional) || unit == 1;
        boolean unitsMoreOrEqualsThanOne = unit > 0;
        return (lastDigitIsOneOrTwoOtZero && exceptSimpleNumeric && f) || (unitsMoreOrEqualsThanOne && isZero);
    }

    private void changeEndsOfScale(int twoLastSigns, int unit, StringBuilder stringBuilder) {
        if (unit == 3) {
            if (!isLessThanTwenty(twoLastSigns) || twoLastSigns < 5) {
                switch (twoLastSigns - twoLastSigns / 10 * 10) {
                    case 1 -> stringBuilder.append("а");
                    case 2, 3, 4 -> stringBuilder.append("и");
                }
            }
        } else if (unit > 3) {
            if (!isLessThanTwenty(twoLastSigns) || twoLastSigns < 5) {
                switch (twoLastSigns - twoLastSigns / 10 * 10) {
                    case 1 -> stringBuilder.append("");
                    case 2, 3, 4 -> stringBuilder.append("а");
                    default -> stringBuilder.append("ов");
                }
            } else {
                stringBuilder.append("ов");
            }
        }
    }

    private String convertHundreds(int hundredNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        VariableRecord variableTextFormat = hundredsParser.parse(hundredNumber);
        stringBuilder.append(variableTextFormat.textFormat());
        return stringBuilder.toString();
    }

    private String convertTens(int tens, boolean flagToChangeOnSecondFormSyntax) {
        StringBuilder stringBuilder = new StringBuilder();
        int t = tens / 10;
        int units = tens - t * 10;

        VariableRecord tensVariableTextFormat = tensParser.parse(t);

        appendIfNotBlank(tensVariableTextFormat.textFormat(), stringBuilder);
        appendIfNotBlank(getSimpleNumeral(units, flagToChangeOnSecondFormSyntax), stringBuilder);
        return stringBuilder.toString();
    }

    private void addEndingOfWholePartNumeric(String wholePart, StringBuilder stringBuilder) {
        int endsTens = 0;
        if (wholePart.length() >= 2) {
            endsTens = Integer.parseInt(wholePart.substring(wholePart.length() - 2));
        }
        int ends = Integer.parseInt(wholePart.substring(wholePart.length() - 1));

        if (ends == 1 && endsTens != 11) {
            stringBuilder.append(POINT_WITH_CHANGED_ENDS).append(" ");
        } else {
            stringBuilder.append(POINT).append(" ");
        }
    }

    private String getFractionalPart(Number numberToConvert) {
        if (isNotAFractionalNumber(numberToConvert)) {
            return "";
        }
        return numberToConvert.toString().split(SEPARATOR)[1];
    }

    private String getWholePart(Number numberToConvert) {
        return numberToConvert.toString().split(SEPARATOR)[0];
    }

    private String getSimpleNumeral(int number, boolean flagToChangeOnSecondFormSyntax) {
        StringBuilder buffer = new StringBuilder();
        VariableRecord variableTextFormat = variableParser.parse(number);
        if (flagToChangeOnSecondFormSyntax) {
            buffer.append(variableTextFormat.variable());
        } else {
            buffer.append(variableTextFormat.textFormat());
        }
        return buffer.toString();
    }

    private String getUnitsWordsOfFractionPart(String fractionalPart) {
        StringBuilder stringBuilder = new StringBuilder();
        String placesScale = unitParser.parse((fractionalPart.length()) / 3).textFormat();
        int scFactor = fractionalPart.length() < 3 ? 0 : 3;
        ScaleRecord unitRecord = switch (fractionalPart.length() % 3) {
            case 1 -> unitFactorParser.parse(1 + scFactor);
            case 2 -> unitFactorParser.parse(2 + scFactor);
            default -> new ScaleRecord(0, "");
        };
        String prePlacesScale = unitRecord.textFormat();
        int lastDigits = fractionalPart.length() >= 2 ?
                Integer.parseInt(fractionalPart.substring(fractionalPart.length() - 2)) :
                Integer.parseInt(fractionalPart);
        String changed = getModifiedEndingsOfTheFractionalPartOfUnits(lastDigits, prePlacesScale, placesScale, unitRecord.scale());
        stringBuilder.append(changed);
        return stringBuilder.toString();
    }

    private String getModifiedEndingsOfTheFractionalPartOfUnits(int twoLastDigits, String pre, String post, int unit) {
        String ends;
        StringBuilder stringBuilder = new StringBuilder(pre).append(post);
        int lastDigit = twoLastDigits - twoLastDigits / 10 * 10;
        if (lastDigit == 1) {
            if (!isLessThanTwenty(twoLastDigits) || twoLastDigits < 2) {
                switch (unit) {
                    case -1, -2, -3 -> stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), "ая");
                    default -> stringBuilder.append("ная");
                }
            }
        } else if (unit >= 0 || unit<=-4 ) {
            stringBuilder.append("ных");
        }
        ends = stringBuilder.toString();
        return ends;
    }

    private boolean isLessThanTwenty(Number numberToConvert) {
        return numberToConvert.doubleValue() < 20;
    }

    private boolean isANegativeNumber(String number) {
        return number.startsWith("-");
    }

    private boolean isNotAFractionalNumber(Number number) {
        return number.toString().split(SEPARATOR).length < 2;
    }

    private boolean isAFractionalNumber(Number number) {
        String[] partOfNumber = number.toString().split(SEPARATOR);
        boolean b = partOfNumber.length == 2;
        boolean i = false;
        if (b) i = !getNumberWithoutLastZeros(partOfNumber[1]).isBlank();
        return b && i;
    }

    private void shutdownAndAwaitTermination(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void appendIfNotBlank(String stringToAppend, StringBuilder stringBuilder) {
        if (!stringToAppend.isBlank()) {
            stringBuilder.append(stringToAppend).append(" ");
        }
    }
}