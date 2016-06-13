package ru.biserhobby.fronton.core;

import java.util.stream.Stream;

public class Project implements Runnable {
	private final Stream<? extends Runnable> topDirectives;

	public Project(Stream<? extends Runnable> topDirectives){
		Utils.checkArgumentNotNull(topDirectives, "topDirectives");
		this.topDirectives = topDirectives;
	}

	@Override
	public void run() throws FrontonException{
		topDirectives.parallel().forEach(Runnable::run);
	}
}
