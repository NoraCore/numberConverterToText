package by.jazzTeam.csvWorkers;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class InstanceValidator<T> {
    public void validate(Class<T> className, Set<String> columns) {
        checkArgument(columns.size() == className.getDeclaredFields().length,
                "Incompatible types.");

        List<String> fields = Stream.of(className.getDeclaredFields())
                .map(field -> {
                    return field.getName()
                            .toLowerCase(Locale.ROOT);
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        List<String> colStrings = columns.stream()
                .map(column -> {
                    return column
                            .replaceAll("[_ ]", "")
                            .toLowerCase(Locale.ROOT);
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        checkArgument(fields.equals(colStrings), "Incompatible types.");
    }
}
