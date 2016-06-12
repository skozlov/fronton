package ru.biserhobby.fronton.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SimplePage implements Runnable {
	private final Path source;
	private final Path target;

	public SimplePage(Path source, Path target) {
		Utils.checkArgumentNotNull(source, "source");
		Utils.checkArgumentNotNull(target, "target");
		this.source = source;
		this.target = target;
	}

	@Override
	public void run() throws FrontonIOException{
		if(!source.equals(target)) {
			try {
				Files.copy(source, target, StandardCopyOption.values());
			} catch (IOException e) {
				throw new FrontonIOException(e);
			}
		}
	}
}
