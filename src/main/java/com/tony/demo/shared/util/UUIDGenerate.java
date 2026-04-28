package com.tony.demo.shared.util;

import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

public class UUIDGenerate {
    public static UUID nextUuid() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
