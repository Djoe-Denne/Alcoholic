package com.djden.alcoholic.api.data;

import com.djden.alcoholic.api.PublicApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Minimal JSON parser that produces {@link DataNode} trees without Gson or Minecraft.
 */
@PublicApi
public final class JsonDataParser {
    private final String source;
    private int index;

    private JsonDataParser(String source) {
        this.source = source;
    }

    public static DataNode parse(String json) {
        JsonDataParser parser = new JsonDataParser(Objects.requireNonNull(json, "json"));
        DataNode node = parser.parseValue();
        parser.skipWhitespace();
        if (!parser.done()) {
            throw parser.error("unexpected trailing input");
        }
        return node;
    }

    private DataNode parseValue() {
        skipWhitespace();
        if (done()) {
            throw error("unexpected end of input");
        }
        char current = peek();
        if (current == '{') {
            return parseObject();
        }
        if (current == '[') {
            return parseArray();
        }
        if (current == '"') {
            return DataNode.string(parseString());
        }
        if (current == 't' || current == 'f') {
            return DataNode.bool(parseLiteral("true", true) || parseCheckedFalse());
        }
        if (current == 'n') {
            parseLiteral("null", true);
            return DataNode.nil();
        }
        if (current == '-' || isDigit(current)) {
            return DataNode.number(parseNumber());
        }
        throw error("unexpected character '" + current + "'");
    }

    private DataNode parseObject() {
        expect('{');
        Map<String, DataNode> fields = new LinkedHashMap<>();
        skipWhitespace();
        if (consume('}')) {
            return DataNode.object(fields);
        }
        while (true) {
            skipWhitespace();
            String name = parseString();
            skipWhitespace();
            expect(':');
            fields.put(name, parseValue());
            skipWhitespace();
            if (consume('}')) {
                return DataNode.object(fields);
            }
            expect(',');
        }
    }

    private DataNode parseArray() {
        expect('[');
        List<DataNode> values = new ArrayList<>();
        skipWhitespace();
        if (consume(']')) {
            return DataNode.list(values);
        }
        while (true) {
            values.add(parseValue());
            skipWhitespace();
            if (consume(']')) {
                return DataNode.list(values);
            }
            expect(',');
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (!done()) {
            char current = next();
            if (current == '"') {
                return builder.toString();
            }
            if (current != '\\') {
                builder.append(current);
                continue;
            }
            if (done()) {
                throw error("unterminated escape");
            }
            char escaped = next();
            builder.append(switch (escaped) {
                case '"', '\\', '/' -> escaped;
                case 'b' -> '\b';
                case 'f' -> '\f';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 't' -> '\t';
                case 'u' -> parseUnicode();
                default -> throw error("invalid escape '" + escaped + "'");
            });
        }
        throw error("unterminated string");
    }

    private char parseUnicode() {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            if (done()) {
                throw error("unterminated unicode escape");
            }
            char digit = next();
            value <<= 4;
            if (digit >= '0' && digit <= '9') {
                value += digit - '0';
            } else if (digit >= 'a' && digit <= 'f') {
                value += digit - 'a' + 10;
            } else if (digit >= 'A' && digit <= 'F') {
                value += digit - 'A' + 10;
            } else {
                throw error("invalid unicode digit '" + digit + "'");
            }
        }
        return (char) value;
    }

    private Number parseNumber() {
        int start = index;
        if (peek() == '-') {
            next();
        }
        if (done() || !isDigit(peek())) {
            throw error("invalid number");
        }
        if (peek() == '0') {
            next();
        } else {
            while (!done() && isDigit(peek())) {
                next();
            }
        }
        if (!done() && peek() == '.') {
            next();
            if (done() || !isDigit(peek())) {
                throw error("invalid fractional number");
            }
            while (!done() && isDigit(peek())) {
                next();
            }
        }
        if (!done() && (peek() == 'e' || peek() == 'E')) {
            next();
            if (!done() && (peek() == '+' || peek() == '-')) {
                next();
            }
            if (done() || !isDigit(peek())) {
                throw error("invalid exponent");
            }
            while (!done() && isDigit(peek())) {
                next();
            }
        }
        String lexeme = source.substring(start, index);
        if (lexeme.indexOf('.') >= 0 || lexeme.indexOf('e') >= 0 || lexeme.indexOf('E') >= 0) {
            return Double.parseDouble(lexeme);
        }
        long parsed = Long.parseLong(lexeme);
        if (parsed >= Integer.MIN_VALUE && parsed <= Integer.MAX_VALUE) {
            return (int) parsed;
        }
        return parsed;
    }

    private boolean parseCheckedFalse() {
        parseLiteral("false", true);
        return false;
    }

    private boolean parseLiteral(String literal, boolean required) {
        if (source.startsWith(literal, index)) {
            index += literal.length();
            return true;
        }
        if (required) {
            throw error("expected '" + literal + "'");
        }
        return false;
    }

    private void skipWhitespace() {
        while (!done()) {
            char current = peek();
            if (current != ' ' && current != '\n' && current != '\r' && current != '\t') {
                return;
            }
            index++;
        }
    }

    private void expect(char expected) {
        if (!consume(expected)) {
            throw error("expected '" + expected + "'");
        }
    }

    private boolean consume(char expected) {
        if (!done() && peek() == expected) {
            index++;
            return true;
        }
        return false;
    }

    private char peek() {
        return source.charAt(index);
    }

    private char next() {
        return source.charAt(index++);
    }

    private boolean done() {
        return index >= source.length();
    }

    private static boolean isDigit(char value) {
        return value >= '0' && value <= '9';
    }

    private DataDecodeException error(String message) {
        return new DataDecodeException("$", message + " at index " + index);
    }
}
