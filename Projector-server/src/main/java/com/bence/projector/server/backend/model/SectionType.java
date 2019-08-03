package com.bence.projector.server.backend.model;

import java.util.HashMap;
import java.util.Map;

public enum SectionType {
    INTRO(0),
    VERSE(1),
    PRE_CHORUS(2),
    CHORUS(3),
    BRIDGE(4),
    CODA(5);
    private static Map<Integer, SectionType> sectionTypeMap;
    private final int value;

    SectionType(int value) {
        this.value = value;
    }

    public static SectionType getInstance(int value) {
        Map<Integer, SectionType> sectionTypeMap = getSectionTypeMap();
        return sectionTypeMap.get(value);
    }

    private static Map<Integer, SectionType> getSectionTypeMap() {
        if (sectionTypeMap == null) {
            SectionType[] values = SectionType.values();
            sectionTypeMap = new HashMap<>(values.length);
            for (SectionType sectionType : values) {
                sectionTypeMap.put(sectionType.getValue(), sectionType);
            }
        }
        return sectionTypeMap;
    }

    public int getValue() {
        return value;
    }
}
