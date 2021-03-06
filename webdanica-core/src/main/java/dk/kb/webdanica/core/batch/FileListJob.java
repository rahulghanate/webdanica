package dk.kb.webdanica.core.batch;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.batch.FileBatchJob;

/**
 * A batch job which returns a list of all files in the bitarchive in which it
 * runs.
 */
public class FileListJob extends FileBatchJob {
    
    /** The class logger. This variable is re-initialized during 
     * de-serialization.
     */
    protected transient Log log = LogFactory.getLog(getClass().getName());

    /** The constructor. */
    public FileListJob() {
        // Keep the batchJobTimeout at default (-1) so it will be overridden 
        // by the settings for default batch timeout.
    }
    
    /**
     * Initializes fields in this class.
     * @param os the OutputStream to which data is to be written
     */
    public void initialize(OutputStream os) {
    }

    /**
     * Invoke default method for deserializing object, and reinitialise the
     * logger.
     * @param s the ObjectInputStream from which the object is read
     */
    private void readObject(ObjectInputStream s) {
        try {
            s.defaultReadObject();
        } catch (Exception e) {
            throw new IOFailure("Unexpected error during deserialization", e);
        }
        log = LogFactory.getLog(getClass().getName());
    }

    /**
     * Writes the name of the arcfile to the OutputStream.
     * @param file an arcfile
     * @param os the OutputStream to which data is to be written
     * @return false If listing of this arcfile fails; otherwise true
     */
    public boolean processFile(File file, OutputStream os) {
        ArgumentNotValid.checkNotNull(file, "file");
        String result = file.getName() + "\n";
        try {
            os.write(result.getBytes());
        } catch (IOException e) {
            log.warn("Listing of file " + file.getName()
                    + " failed: ", e);
            return false;
        }
        return true;
    }

    /**
     * Does nothing.
     * @param os the OutputStream to which data is to be written
     */
    public void finish(OutputStream os) {
    }

    /**
     * Return a human-readable representation of a FileListJob.
     * @return a human-readable representation of a FileListJob
     */
    public String toString() {
        int filesFailedCount;
        if (filesFailed == null) {
            filesFailedCount = 0;
        } else {
            filesFailedCount = filesFailed.size();
        }
        return ("\nFileList job:\nFiles Processed = "
                + noOfFilesProcessed
                + "\nFiles  failed = " + filesFailedCount);
    }
}
