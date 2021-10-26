package com.example.workflow.services;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class FilePathService {

	public String getQualifiedFilePath(String path, Class c) {
		return path.concat("/")
				.concat(getFileNameInRepo(c).toLowerCase(Locale.ROOT)
						.concat(".json"));
	}

	private String getFileNameInRepo(Class obj) {
		return obj.getSimpleName();
	}
}
