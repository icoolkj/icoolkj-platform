package com.icoolkj.platform.generator.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Writes generated JSON in the same style as frontend Prettier / JSON.stringify(..., 2):
 * LF newlines, no space before colon, one space after colon, and multiline arrays.
 */
final class GenJsonWriter {

    /** Always LF so Windows hosts still match frontend {@code endOfLine: "lf"}. */
    private static final DefaultIndenter LF_INDENTER = new DefaultIndenter("  ", "\n");

    private GenJsonWriter() {
    }

    static String write(ObjectMapper mapper, JsonNode node) throws JsonProcessingException {
        String json = mapper.writer(new StandardPrettyPrinter()).writeValueAsString(node);
        // Match JSON.stringify: {} / [] without inner space (Jackson DefaultPrettyPrinter uses "{ }")
        String normalized = json.replace("{ }", "{}").replace("[ ]", "[]");
        return GenTextNormalize.toLfWithTrailingNewline(normalized);
    }

    private static final class StandardPrettyPrinter extends DefaultPrettyPrinter {

        private StandardPrettyPrinter() {
            indentObjectsWith(LF_INDENTER);
            indentArraysWith(LF_INDENTER);
        }

        private StandardPrettyPrinter(StandardPrettyPrinter base) {
            super(base);
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new StandardPrettyPrinter(this);
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator generator) throws IOException {
            generator.writeRaw(": ");
        }
    }
}
