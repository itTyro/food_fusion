package com.linzhilong.test;

import org.junit.jupiter.api.Test;

import java.util.UUID;

public class TestPath {
    @Test
    public void testPath() {
        String fileName = "ifjaidjds.jpg";

        String suffix = fileName.substring(fileName.lastIndexOf("."));

        String newPathName = UUID.randomUUID().toString() + suffix;

        System.out.println(newPathName);
    }
}
