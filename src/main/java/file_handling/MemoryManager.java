/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file_handling;

/**
 * Memory Management
 *
 * @author Fábio Ferreira
 * @author Rui Frazão
 */
public class MemoryManager {
    
    private long totalMemory;
    private long freeMemory;
    private long usedMemory;

    public long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public long getUsedMemory() {
        return getTotalMemory()-getFreeMemory();
    }

    /**
     * Calculates memory usage using the Runtime Java API
     * 
     * @return Used Memory Percentage;
     */
    public float getMemoryUsage() {
        Runtime rt = Runtime.getRuntime();
        totalMemory = rt.totalMemory();
        freeMemory = rt.freeMemory();
        usedMemory = totalMemory - freeMemory;
        return (float) usedMemory / totalMemory * 100;
    }
}
