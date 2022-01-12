package br.ufsc.mov3lets.method.discovery.structures;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * Updated Thread safety for read/write operations.
 * 
 * @author Tarlis Tortelli Portela
 *
 */
public class Lock {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReadLock readLock = rwLock.readLock();
    private final WriteLock writeLock = rwLock.writeLock();
    
    public ReadLock getReadLock() {
		return readLock;
	}
    
    public WriteLock getWriteLock() {
		return writeLock;
	}

}
