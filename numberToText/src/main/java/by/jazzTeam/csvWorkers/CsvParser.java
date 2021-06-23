package by.jazzTeam.csvWorkers;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.stream.Collectors;

public class CsvParser<T> {
    private final CsvReader reader;
    private final ParseStrategy<T> strategy;


    public CsvParser(CsvReader reader,ParseStrategy strategy) {
        this.reader = reader;
        this.strategy = strategy;
    }

    public List<T> parseAll() {
        return reader.readAll().stream()
                .map(mapOfValue -> (T)strategy.instantiate(mapOfValue)).collect(Collectors.toList());
    }


    public T parse(int numOfLines) {
        return (T) strategy.instantiate(reader.read(numOfLines));
    }
}
