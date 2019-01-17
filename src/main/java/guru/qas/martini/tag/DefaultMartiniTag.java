/*
Copyright 2017-2019 Penny Rohr Curich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package guru.qas.martini.tag;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Objects;

import gherkin.pickles.PickleTag;
import exception.MartiniException;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class DefaultMartiniTag implements MartiniTag {

	protected final String name;
	protected final String argument;

	public String getName() {
		return name;
	}

	public String getArgument() {
		return argument;
	}

	protected DefaultMartiniTag(String name, String argument) {
		this.name = name;
		this.argument = argument;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DefaultMartiniTag)) {
			return false;
		}
		DefaultMartiniTag that = (DefaultMartiniTag) o;
		return Objects.equal(getName(), that.getName()) &&
			Objects.equal(getArgument(), that.getArgument());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getName(), getArgument());
	}

	public static Builder builder() {
		return new Builder();
	}

	@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
	public static class Builder {

		protected static final Pattern PATTERN_SIMPLE = Pattern.compile("^@(.+)$");
		protected static final Pattern PATTERN_ARGUMENTED = Pattern.compile("^@(.+)\\(\"(.+)\"\\)$");

		protected PickleTag pickleTag;

		protected Builder() {
		}

		public Builder setPickleTag(PickleTag t) {
			this.pickleTag = t;
			return this;
		}

		public DefaultMartiniTag build() {
			checkState(null != pickleTag, "PickleTag not set");

			DefaultMartiniTag tag = getArgumented().orElseGet(this::getSimple);
			if (null == tag) {
				String input = getInput();
				throw new MartiniException(DefaultMartiniTagMessages.ILLEGAL_SYNTAX, input);
			}
			return tag;
		}

		protected String getInput() {
			String name = pickleTag.getName();
			return name.trim();
		}

		protected Optional<DefaultMartiniTag> getArgumented() {
			String input = getInput();
			Matcher matcher = PATTERN_ARGUMENTED.matcher(input);
			DefaultMartiniTag tag = null;
			if (matcher.find()) {
				String name = matcher.group(1);
				String argument = matcher.group(2);
				tag = new DefaultMartiniTag(name, argument);
			}
			return Optional.ofNullable(tag);
		}

		protected DefaultMartiniTag getSimple() {
			String input = getInput();
			Matcher matcher = PATTERN_SIMPLE.matcher(input);
			DefaultMartiniTag tag = null;
			if (matcher.find()) {
				String name = matcher.group(1);
				tag = new DefaultMartiniTag(name, null);
			}
			return tag;
		}
	}
}
