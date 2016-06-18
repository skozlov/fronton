package ru.biserhobby.fronton.core;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface CustomAttributeProcessor {
	Optional<String> mapCustom(String value);

	Optional<String> mapTarget(String customValue, Optional<String> targetValue);

	static CustomAttributeProcessor fromMappers(
			Function<String, Optional<String>> customMapper,
			BiFunction<String, Optional<String>, Optional<String>> targetMapper){

		Utils.checkArgumentNotNull(customMapper, "customMapper");
		Utils.checkArgumentNotNull(targetMapper, "targetMapper");
		return new CustomAttributeProcessor() {
			@Override
			public Optional<String> mapCustom(String value) {
				return customMapper.apply(value);
			}

			@Override
			public Optional<String> mapTarget(String customValue, Optional<String> targetValue) {
				return targetMapper.apply(customValue, targetValue);
			}
		};
	}

	Function<String, Optional<String>> LEAVE_CUSTOM_UNCHANGED = Optional::of;

	Function<String, Optional<String>> REMOVE_CUSTOM = s -> Optional.empty();

	BiFunction<String, Optional<String>, Optional<String>> LEAVE_TARGET_UNCHANGED = (custom, target) -> target;

	BiFunction<String, Optional<String>, Optional<String>> COPY_CUSTOM_TO_TARGET = (custom, target) -> Optional.of(custom);
}
