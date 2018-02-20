/*
Copyright 2017 Penny Rohr Curich

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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;

import guru.qas.martini.Martini;

@SuppressWarnings("WeakerAccess")
public class TagResolver implements MethodResolver {

	protected static final Pattern PATTERN = Pattern.compile("^is(\\S+)$");

	protected final ApplicationContext applicationContext;
	protected final Categories categories;

	public TagResolver(ApplicationContext applicationContext, Categories categories) {
		this.applicationContext = applicationContext;
		this.categories = categories;
	}

	@Override
	public MethodExecutor resolve(
		@Nonnull EvaluationContext context,
		@Nonnull Object targetObject,
		@Nonnull String name,
		@Nonnull List<TypeDescriptor> argumentTypes
	) throws AccessException {
		return Martini.class.isInstance(targetObject) ? resolve(name, argumentTypes) : null;
	}

	protected MethodExecutor resolve(String name, List<TypeDescriptor> argumentTypes) {
		return null == argumentTypes || isMatch(argumentTypes) ? resolve(name) : null;
	}

	protected boolean isMatch(List<TypeDescriptor> argumentTypes) {
		return argumentTypes.isEmpty() || (1 == argumentTypes.size() && isMatch(argumentTypes.get(0)));
	}

	protected boolean isMatch(TypeDescriptor descriptor) {
		Class<?> objectType = descriptor.getObjectType();
		return String.class.equals(objectType);
	}

	protected MethodExecutor resolve(String name) {
		Matcher matcher = PATTERN.matcher(name);
		return matcher.find() ? resolve(matcher) : null;
	}

	protected MethodExecutor resolve(Matcher matcher) {
		String group = matcher.group(1);

		MethodExecutor executor;
		switch (group) {
			case "Category":
				executor = new CategoryExecutor(categories);
				break;
			case "Resource":
				executor = new ResourceExecutor(applicationContext);
				break;
			default:
				executor = new TagExecutor(group);
				break;
		}
		return executor;
	}
}
