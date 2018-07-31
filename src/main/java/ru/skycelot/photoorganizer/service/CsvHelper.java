package ru.skycelot.photoorganizer.service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CsvHelper {

    public String encodeFields(List<String> fields) {
        return fields.stream().map(field -> {
            if (field.contains(",") || field.contains("\"")) {
                field = "\"" + field.replace("\"", "\"\"") + "\"";
            }
            return field;
        }).collect(Collectors.joining(","));
    }

    public List<String> decodeFields(String line) {
        List<String> fields = new LinkedList<>();
        StringBuilder fieldValue = new StringBuilder();
        State state= State.FIELD_END;
        for (int offset = 0; offset < line.length(); offset++) {
            char csvChar = line.charAt(offset);
            switch (csvChar) {
                case '"':
                    switch (state) {
                        case FIELD_END:
                            state = State.QUOTED_FIELD;
                            break;
                        case FIELD:
                            throw new IllegalArgumentException("Fields with double quotes must be surrounded with double quotes");
                        case QUOTED_FIELD:
                            state = State.QUOTE_IN_FIELD;
                            break;
                        case QUOTE_IN_FIELD:
                            state = State.QUOTED_FIELD;
                            fieldValue.append(csvChar);
                            break;
                    }
                    break;
                case ',':
                    switch (state) {
                        case FIELD_END:
                            fields.add("");
                            break;
                        case FIELD:
                            state = State.FIELD_END;
                            fields.add(fieldValue.toString());
                            fieldValue.setLength(0);
                            break;
                        case QUOTED_FIELD:
                            fieldValue.append(csvChar);
                            break;
                        case QUOTE_IN_FIELD:
                            state = State.FIELD_END;
                            fields.add(fieldValue.toString());
                            fieldValue.setLength(0);
                            break;
                    }
                    break;
                default:
                    switch (state) {
                        case FIELD_END:
                            state = State.FIELD;
                            fieldValue.append(csvChar);
                            break;
                        case FIELD:
                            fieldValue.append(csvChar);
                            break;
                        case QUOTED_FIELD:
                            fieldValue.append(csvChar);
                            break;
                    }
            }
        }
        switch (state) {
            case FIELD_END:
                fields.add("");
                break;
            case FIELD:
            case QUOTE_IN_FIELD:
                fields.add(fieldValue.toString());
                break;
            case QUOTED_FIELD:
                throw new IllegalArgumentException("Field that is started with double quote must be ended with double quote");
        }
        return fields;
    }

    private static enum State {
        FIELD_END, FIELD, QUOTED_FIELD, QUOTE_IN_FIELD
    }
}
