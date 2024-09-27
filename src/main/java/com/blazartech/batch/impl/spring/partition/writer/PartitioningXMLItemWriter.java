/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.partition.writer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * An item writer that will write out XML objects, via JAXB, to different files
 * depending on the partition to which the object belongs.
 *
 * @author aar1069
 * @param <T> the type of object being written
 */
public class PartitioningXMLItemWriter<T> implements ItemWriter<T>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(PartitioningXMLItemWriter.class);

    private String outputDirectory;
    private String fileNameBase;

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getFileNameBase() {
        return fileNameBase;
    }

    public void setFileNameBase(String fileNameBase) {
        this.fileNameBase = fileNameBase;
    }

    private final List<File> outputFiles = new ArrayList<>();

    private Jaxb2Marshaller marshaller;

    public Jaxb2Marshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(Jaxb2Marshaller jaxbMarshaller) {
        this.marshaller = jaxbMarshaller;
    }
    private IDeterminePartition<T> determinePartition;

    public IDeterminePartition getDeterminePartition() {
        return determinePartition;
    }

    public void setDeterminePartition(IDeterminePartition<T> determinePartition) {
        this.determinePartition = determinePartition;
    }

    private IObjectSet<T> defaultObjectSet;

    public IObjectSet<T> getDefaultObjectSet() {
        return defaultObjectSet;
    }

    public void setDefaultObjectSet(IObjectSet<T> defaultObjectSet) {
        this.defaultObjectSet = defaultObjectSet;
    }

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        Map<Integer, List<T>> hashedList = new HashMap<>();

	chunk.getItems().forEach(a -> {
		// determine the partition to which this object belongs.
		int partitionNumber = determinePartition.determinePartition(a);
		logger.debug("partition " + partitionNumber);
		
		// save.
		List<T> partitionedObjectList = hashedList.get(partitionNumber);
		if (partitionedObjectList == null) {
		    partitionedObjectList = new ArrayList<>();
		    hashedList.put(partitionNumber, partitionedObjectList);
		}
		partitionedObjectList.add(a);
	    });

        // send each list to the appropriate file.
        for (int i : hashedList.keySet()) {
            // determine the output file.
            File outputFile = outputFiles.get(i);
            Result fileResult = new StreamResult(outputFile);
            Source fileSource = new StreamSource(outputFile);

            // read the current content of the file into an object.
            IObjectSet<T> objectSet;
            if (outputFile.length() > 0) {
                objectSet = (IObjectSet<T>) marshaller.unmarshal(fileSource);
            } else {
                // create a new object set, but how?
                objectSet = defaultObjectSet.createNew();
                objectSet.setObjects(new ArrayList<>());
            }

            // add the new values.
            objectSet.getObjects().addAll(hashedList.get(i));

            // write.
            marshaller.marshal(objectSet, fileResult);
        }
    }

    private File buildFile(int i) {
        return new File(getOutputDirectory(), getFileNameBase() + "-" + i + ".xml");
    }

    private boolean overwriteFiles = false;

    public boolean isOverwriteFiles() {
        return overwriteFiles;
    }

    public void setOverwriteFiles(boolean overwriteFiles) {
        this.overwriteFiles = overwriteFiles;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("afterPropertiesSet...");

        /* initialize the list of files.  if the overwriteFile flag is set to 
         true, then create a brand-new file.  Otherwise, don't.
         */
        for (int i = 0; i < 10; i++) {
            File outputFile = buildFile(i);
            if (overwriteFiles) {
                if (outputFile.exists()) {
                    outputFile.delete();
                    outputFile.createNewFile();
                }
            }
            outputFiles.add(outputFile);
        }
    }
}
