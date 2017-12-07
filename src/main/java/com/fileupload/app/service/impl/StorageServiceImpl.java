/**
 * 
 */
package com.fileupload.app.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fileupload.app.exceptions.StorageException;
import com.fileupload.app.exceptions.StorageFileNotFoundException;
import com.fileupload.app.service.StorageService;

@Service
@PropertySource("classpath:application.properties")
public class StorageServiceImpl implements StorageService {

	
	private final Path rootLocation;

	@Autowired
	public StorageServiceImpl() throws IOException {
		this.rootLocation = Paths.get("files");
		Files.createDirectories(rootLocation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fileupload.app.service.StorageService#store(org.springframework.web.
	 * multipart.MultipartFile)
	 */
	@Override
	public void store(MultipartFile file) {
		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file " + filename);
			}
			if (filename.contains("..")) {
				// This is a security check
				throw new StorageException(
						"Cannot store file with relative path outside current directory " + filename);
			}
			Files.copy(file.getInputStream(), this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new StorageException("Failed to store file " + filename, e);
		}
	}

	@Override
	public Stream<Path> loadAll() {
		try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
	}

	@Override
	public Resource loadAsResource(String filename) {
		 try {
	            Path file = load(filename);
	            Resource resource = new UrlResource(file.toUri());
	            if (resource.exists() || resource.isReadable()) {
	                return resource;
	            }
	            else {
	                throw new StorageFileNotFoundException(
	                        "Could not read file: " + filename);

	            }
	        }
	        catch (MalformedURLException e) {
	            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
	        }
	}
	
	    public Path load(String filename) {
	        return rootLocation.resolve(filename);
	    }

}
