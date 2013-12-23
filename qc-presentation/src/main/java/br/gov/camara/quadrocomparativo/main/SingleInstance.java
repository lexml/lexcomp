package br.gov.camara.quadrocomparativo.main;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleInstance {
	
	
	private static final Logger logger = Logger.getLogger(SingleInstance.class.getName());

	private static final String TEMP_FILE_NAME = "lexcomp_lock";
	private static final String TEMP_FILE_NAME_SUFIX = "tmp";
	
	public static boolean lockInstance() {

        final File file = new File(System.getProperty("java.io.tmpdir")+"/"+TEMP_FILE_NAME+"."+TEMP_FILE_NAME_SUFIX);
		try {
	        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
	        final FileLock fileLock = randomAccessFile.getChannel().tryLock();
	        
	        if (fileLock != null) {
	        	logger.log(Level.ALL, "Locked: "+file);
	        	
	            Runtime.getRuntime().addShutdownHook(new Thread() {
	                public void run() {
	                    try {
	                        fileLock.release();
	                        randomAccessFile.close();
	                        file.delete();
	                    } catch (Exception e) {
	                    	logger.log(Level.SEVERE, "Unable to remove lock file: " + file, e);
	                    }
	                }
	            });
	            return true;
	        }
	    } catch (Exception e) {
	    	logger.log(Level.SEVERE, "Unable to create and/or lock file.", e);
	    }

		logger.log(Level.ALL, "Could not lock: "+file);
		
	    return false;
	}
	
}
