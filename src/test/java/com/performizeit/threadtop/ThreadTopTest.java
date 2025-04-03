package com.performizeit.threadtop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import picocli.CommandLine;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for ThreadTop functionality with JUnit 5
 */
public class ThreadTopTest {

    @Test
    @DisplayName("Test command line parsing with Picocli")
    void testCommandLineParsing() {
        ThreadTopOptions opts = new ThreadTopOptions();
        CommandLine cmd = new CommandLine(opts);
        
        // Parse args
        cmd.parseArgs("-m", "1000", "-c", "-t", "1234");
        
        // Verify options are correctly parsed
        assertEquals(1000, opts.getTimeToMeasure(), "Time to measure should be 1000ms");
        assertTrue(opts.isMeasureThreadCPU(), "CPU measurement should be enabled");
        assertTrue(opts.isPrintStackTrace(), "Stack trace printing should be enabled");
        assertEquals(List.of("1234"), opts.getConectionStringList(), "Connection list should contain 1234");
    }
    
    @Test
    @DisplayName("Test default option values")
    void testDefaultValues() {
        ThreadTopOptions opts = new ThreadTopOptions();
        CommandLine cmd = new CommandLine(opts);
        
        // Parse empty args
        cmd.parseArgs();
        
        // Verify default values
        assertEquals(5000, opts.getTimeToMeasure(), "Default time to measure should be 5000ms");
        assertEquals(1, opts.getIterations(), "Default iterations should be 1");
        assertEquals(10, opts.getNum(), "Default number of threads should be 10");
        assertEquals("CPU", opts.getSort(), "Default sort should be CPU");
        assertEquals(".*", opts.getRegExp(), "Default regexp should be .*");
        assertEquals(0, opts.getStackTraceEntriesNo(), "Default stack trace entries should be 0");
    }
}
