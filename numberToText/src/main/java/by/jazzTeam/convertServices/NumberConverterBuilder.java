package by.jazzTeam.convertServices;

import by.jazzTeam.PropertiesLoader;
import by.jazzTeam.csvWorkers.CsvParser;
import by.jazzTeam.csvWorkers.CsvReader;
import by.jazzTeam.csvWorkers.InstanceValidator;
import by.jazzTeam.csvWorkers.ParseStrategy;
import by.jazzTeam.model.ScaleRecord;
import by.jazzTeam.model.VariableRecord;

public class NumberConverterBuilder {

    private static final String fileScale = PropertiesLoader.get("PATH_TO_SCALE");
    private static final String fileVariable = PropertiesLoader.get("PATH_TO_VARIABLE");
    private static final String fileScaleFactor = PropertiesLoader.get("PATH_TO_SCALE_FACTOR");
    private static final String fileTensNaming = PropertiesLoader.get("PATH_TO_TENS_NAMING");
    private static final String fileHundredsNaming = PropertiesLoader.get("PATH_TO_HUNDREDS_NAMING");


    public static Convertable<Number, String> of(){
        InstanceValidator<VariableRecord> variableValidator = new InstanceValidator<>();
        InstanceValidator<ScaleRecord> unitValidator = new InstanceValidator<>();

        ParseStrategy<ScaleRecord> unitStrategy = new ParseStrategy<>(ScaleRecord.class, unitValidator);
        ParseStrategy<VariableRecord> variableStrategy = new ParseStrategy<>(VariableRecord.class, variableValidator);

        CsvParser<ScaleRecord>unitParser = new CsvParser<>(new CsvReader(fileScale), unitStrategy);
        CsvParser<ScaleRecord>unitFactorParser = new CsvParser<>(new CsvReader(fileScaleFactor), unitStrategy);
        CsvParser<VariableRecord>variableParser = new CsvParser<>(new CsvReader(fileVariable), variableStrategy);
        CsvParser<VariableRecord>tensParser = new CsvParser<>(new CsvReader(fileTensNaming), variableStrategy);
        CsvParser<VariableRecord>hundredsParser = new CsvParser<>(new CsvReader(fileHundredsNaming), variableStrategy);
        final NumberToWordsConverter converter = new NumberToWordsConverter(unitParser, variableParser, tensParser, hundredsParser, unitFactorParser);
        return converter;
    }

    public static Convertable<Number, String> of(String filenameScale, String fileNameScaleFactor, String filenameVariable, String filenameVariableTens, String filenameVariableHundreds){
        InstanceValidator<VariableRecord> variableValidator = new InstanceValidator<>();
        InstanceValidator<ScaleRecord> unitValidator = new InstanceValidator<>();

        ParseStrategy<ScaleRecord> unitStrategy = new ParseStrategy<>(ScaleRecord.class, unitValidator);
        ParseStrategy<VariableRecord> variableStrategy = new ParseStrategy<>(VariableRecord.class, variableValidator);

        CsvParser<ScaleRecord>unitParser = new CsvParser<>(new CsvReader(filenameScale), unitStrategy);
        CsvParser<ScaleRecord>unitFactorParser = new CsvParser<>(new CsvReader(fileNameScaleFactor), unitStrategy);
        CsvParser<VariableRecord>variableParser = new CsvParser<>(new CsvReader(filenameVariable), variableStrategy);
        CsvParser<VariableRecord>tensParser = new CsvParser<>(new CsvReader(filenameVariableTens), variableStrategy);
        CsvParser<VariableRecord>hundredsParser = new CsvParser<>(new CsvReader(filenameVariableHundreds), variableStrategy);


        final NumberToWordsConverter converter = new NumberToWordsConverter(unitParser, variableParser, tensParser, hundredsParser, unitFactorParser);
        return converter;
    }
}
