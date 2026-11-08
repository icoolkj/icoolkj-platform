package com.icoolkj.platform.framework.excel;

import com.icoolkj.platform.common.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelImportI18nTest {

    @Test
    void encodeKeyOnly() {
        assertEquals("error.excel.import_failed", ExcelImportI18n.encode("error.excel.import_failed"));
    }

    @Test
    void encodeWithArgs() {
        String encoded = ExcelImportI18n.encode("error.excel.import_dept_not_found", "研发");
        assertEquals("error.excel.import_dept_not_found" + ExcelImportI18n.ARG_SEP + "研发", encoded);
        assertTrue(ExcelImportI18n.looksLikeKey(encoded));
    }

    @Test
    void encodeBusinessException() {
        BusinessException ex = new BusinessException("error.user.username_exists", "alice");
        assertEquals(
                "error.user.username_exists" + ExcelImportI18n.ARG_SEP + "alice",
                ExcelImportI18n.encode(ex));
    }

    @Test
    void encodeViolationPrefersBraceMessageTemplate() {
        ConstraintViolation<Object> violation = stubViolation("{gen.user.username.required}", "用户名不能为空", null);
        assertEquals("gen.user.username.required", ExcelImportI18n.encodeViolation(violation));
    }

    @Test
    void encodeViolationFallsBackToPropertyPath() {
        ConstraintViolation<Object> violation = stubViolation("must not be blank", "must not be blank", "username");
        assertEquals(
                "error.excel.import_row_invalid" + ExcelImportI18n.ARG_SEP + "username",
                ExcelImportI18n.encodeViolation(violation));
    }

    @Test
    void unwrapBraceKey() {
        assertEquals("gen.user.username.required", ExcelImportI18n.unwrapBraceKey("{gen.user.username.required}"));
        assertEquals("plain", ExcelImportI18n.unwrapBraceKey("plain"));
    }

    @Test
    void looksLikeKeyRejectsLiteral() {
        assertFalse(ExcelImportI18n.looksLikeKey("部门不存在"));
    }

    private static ConstraintViolation<Object> stubViolation(String template, String message, String path) {
        return new ConstraintViolation<>() {
            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public String getMessageTemplate() {
                return template;
            }

            @Override
            public Object getRootBean() {
                return null;
            }

            @Override
            public Class<Object> getRootBeanClass() {
                return Object.class;
            }

            @Override
            public Object getLeafBean() {
                return null;
            }

            @Override
            public Object[] getExecutableParameters() {
                return new Object[0];
            }

            @Override
            public Object getExecutableReturnValue() {
                return null;
            }

            @Override
            public Path getPropertyPath() {
                if (path == null) {
                    return null;
                }
                return new Path() {
                    @Override
                    public java.util.Iterator<Node> iterator() {
                        return java.util.Collections.emptyIterator();
                    }

                    @Override
                    public String toString() {
                        return path;
                    }
                };
            }

            @Override
            public Object getInvalidValue() {
                return null;
            }

            @Override
            public ConstraintDescriptor<?> getConstraintDescriptor() {
                return null;
            }

            @Override
            public <U> U unwrap(Class<U> type) {
                return null;
            }
        };
    }
}
