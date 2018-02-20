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

import java.net.URI;
import java.util.List;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;

import com.google.common.collect.Lists;

import guru.qas.martini.Martini;
import guru.qas.martini.gherkin.FeatureWrapper;
import guru.qas.martini.gherkin.Recipe;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class ResourceExecutor implements MethodExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceExecutor.class);

	protected final ApplicationContext applicationContext;

	public ResourceExecutor(ApplicationContext applicationContext) {
		this.applicationContext = checkNotNull(applicationContext, "null ResourceLoader");
	}

	@Override
	public TypedValue execute(
		@Nonnull EvaluationContext context,
		@Nonnull Object target,
		@Nonnull Object... arguments
	) throws AccessException {
		checkState(1 == arguments.length, "expected a single resource location, found %s", arguments.length);
		Martini martini = Martini.class.cast(target);
		String locationPattern = String.class.cast(arguments[0]);
		return execute(martini, locationPattern);
	}

	public TypedValue execute(Martini martini, String locationPattern) {
		URI martiniURI = getResource(martini);
		boolean evaluation = false;
		if (null != martiniURI) {
			List<Resource> resources = getResources(locationPattern);
			evaluation = resources.stream().map(ResourceExecutor::getURI).anyMatch(martiniURI::equals);
		}

		return new TypedValue(evaluation);
	}

	private static URI getResource(Martini martini) {
		Recipe recipe = martini.getRecipe();
		FeatureWrapper featureWrapper = recipe.getFeatureWrapper();
		Resource resource = featureWrapper.getResource();
		return getURI(resource);
	}

	private static URI getURI(Resource resource) {
		URI uri = null;
		try {
			uri = resource.getURI();
		}
		catch (Exception e) {
			LOGGER.warn("unable to obtain URI from resource {}", resource, e);
		}
		return uri;
	}

	private List<Resource> getResources(String locationPattern) {
		try {
			Resource[] resources = applicationContext.getResources(locationPattern);
			return Lists.newArrayList(resources);
		}
		catch (Exception e) {
			throw new RuntimeException("unable to evaluation resource location " + locationPattern, e);
		}
	}
}
