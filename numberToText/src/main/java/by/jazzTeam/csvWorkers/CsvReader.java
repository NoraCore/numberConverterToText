package by.jazzTeam.csvWorkers;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CsvReader {

    public final String SEPARATOR = ",";
    private final String fileName;

    public CsvReader(String fileName) {
        this.fileName = fileName;
    }

    private Map<String, String> convertInMap(List<String> headers, List<String> values) {
        Iterator<String> iterHeaders = headers.iterator();
        Iterator<String> iterValues = values.iterator();
        Map<String, String> linesWithHeaders = new HashMap<>();

        while (iterHeaders.hasNext()) {
            String header = iterHeaders.next();
            linesWithHeaders.put(header, iterValues.hasNext() ? iterValues.next() : "");
        }
        return linesWithHeaders;
    }

    public List<Map<String, String>> readAll() {
        List<Map<String, String>> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

            List<String> headers = Arrays.asList(reader.readLine().split(SEPARATOR));
            reader.lines().forEach(s -> {
                List<String> values = Arrays.stream(s.split(SEPARATOR))
                        .map(String::trim)
                        .collect(Collectors.toList());
                records.add(convertInMap(headers, values));
            });

        } catch (FileNotFoundException notFoundEx) {
            log.error("{}", notFoundEx.getMessage());
        } catch (IOException ioEx) {
            log.error("{}", ioEx.getMessage());
        }
        return records;
    }

    public List<String> getHeaders() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            return  Arrays.asList(reader.readLine().split(SEPARATOR));
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return Collections.emptyList();
    }

    public Map<String, String> read(int numberOfLine) {

        Map<String, String> mapOfValues;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            List<String> record = new ArrayList<>();
            List<String> headers = Arrays.asList(reader.readLine().split(SEPARATOR));
            reader.lines()
                    .skip(numberOfLine)
                    .findFirst()
                    .ifPresent(s -> record.addAll(
                            Arrays.stream(s.split(SEPARATOR))
                                    .map(String::trim)
                                    .collect(Collectors.toList())));

            mapOfValues = convertInMap(headers, record);
            return mapOfValues;
        } catch (FileNotFoundException notFoundEx) {
            log.error("{}", notFoundEx.getMessage());
        } catch (IOException ioEx) {
            log.error("{}", ioEx.getMessage());
        }
        return Collections.EMPTY_MAP;
    }
}
