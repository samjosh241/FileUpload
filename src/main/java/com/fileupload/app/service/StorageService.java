/**
 * 
 */
package com.fileupload.app.service;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

	void store(MultipartFile file);

	Stream<Path> loadAll();

	Resource loadAsResource(String filename);

}
