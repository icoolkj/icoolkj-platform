package com.icoolkj.platform.generator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Validates page-spec documents against classpath {@code gen-spec/schemas/gen-page-spec.schema.json}
 * (same schema used by {@code gen-spec/validate.mjs}).
 */
public final class PageSpecJsonSchemaValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SCHEMA_CLASSPATH = "gen-spec/schemas/gen-page-spec.schema.json";

    private static volatile JsonSchema pageSpecSchema;

    private PageSpecJsonSchemaValidator() {
    }

    public static List<String> validatePageSpec(Object yamlRootAsJavaObject) {
        List<String> errors = new ArrayList<>();
        try {
            JsonNode node = MAPPER.valueToTree(yamlRootAsJavaObject);
            Set<ValidationMessage> messages = schema().validate(node);
            for (ValidationMessage msg : messages) {
                String path = msg.getInstanceLocation() != null
                        ? msg.getInstanceLocation().toString()
                        : "";
                if (path.isBlank() || "/".equals(path)) {
                    errors.add("page-spec schema: " + msg.getMessage());
                } else {
                    errors.add("page-spec schema: " + path + " " + msg.getMessage());
                }
            }
        } catch (Exception ex) {
            errors.add("page-spec schema validation failed: " + ex.getMessage());
        }
        return errors;
    }

    private static JsonSchema schema() {
        JsonSchema local = pageSpecSchema;
        if (local != null) {
            return local;
        }
        synchronized (PageSpecJsonSchemaValidator.class) {
            if (pageSpecSchema == null) {
                try {
                    ClassPathResource resource = new ClassPathResource(SCHEMA_CLASSPATH);
                    String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
                    pageSpecSchema = factory.getSchema(json);
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to load " + SCHEMA_CLASSPATH, ex);
                }
            }
            return pageSpecSchema;
        }
    }
}
