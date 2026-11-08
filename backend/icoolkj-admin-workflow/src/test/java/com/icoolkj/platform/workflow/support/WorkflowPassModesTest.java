package com.icoolkj.platform.workflow.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorkflowPassModesTest {

    @Test
    void normalizeAndMultiInstance() {
        assertEquals(LinearBpmnBuilder.PASS_SEQUENTIAL, WorkflowPassModes.normalize(null));
        assertEquals(LinearBpmnBuilder.PASS_ANY, WorkflowPassModes.normalize("ANY"));
        assertEquals(LinearBpmnBuilder.PASS_SEQUENTIAL_ALL, WorkflowPassModes.normalize("sequentialAll"));
        assertTrue(WorkflowPassModes.isMultiInstance("all"));
        assertTrue(WorkflowPassModes.isMultiInstance("sequentialAll"));
        assertFalse(WorkflowPassModes.isMultiInstance("sequential"));
    }
}
