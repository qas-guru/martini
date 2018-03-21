/*
Copyright 2017-2018 Penny Rohr Curich

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

package guru.qas.martini;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.context.MessageSource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import guru.qas.martini.gherkin.FeatureWrapper;
import guru.qas.martini.i18n.MessageSources;
import guru.qas.martini.tag.DefaultMartiniTag;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;
import guru.qas.martini.tag.MartiniTag;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of a Martini.
 */
@SuppressWarnings("WeakerAccess")
public class DefaultMartini implements Martini {

	protected final Recipe recipe;
	protected final ImmutableMap<Step, StepImplementation> stepIndex;
	protected final ImmutableSet<MartiniTag> tags;

	@Override
	public Recipe getRecipe() {
		return recipe;
	}

	@Override
	public Map<Step, StepImplementation> getStepIndex() {
		return stepIndex;
	}

	@Override
	public Set<MartiniTag> getTags() {
		return tags;
	}

	public DefaultMartini(
		@Nonnull Recipe recipe,
		@Nonnull Map<Step, StepImplementation> stepIndex,
		@Nonnull Iterable<MartiniTag> tags
	) {
		this.recipe = checkNotNull(recipe, "null Recipe");
		this.stepIndex = ImmutableMap.copyOf(checkNotNull(stepIndex, "null Map"));
		this.tags = ImmutableSet.copyOf(checkNotNull(tags, "null Iterable"));
	}

	@Override
	public String getId() {
		return recipe.getId();
	}

	@Override
	public String getFeatureName() {
		FeatureWrapper feature = getRecipe().getFeatureWrapper();
		return feature.getName();
	}

	@Override
	public String getScenarioName() {
		ScenarioDefinition definition = getRecipe().getScenarioDefinition();
		return definition.getName();
	}

	@Override
	public int getScenarioLine() {
		PickleLocation location = getRecipe().getLocation();
		return location.getLine();
	}

	@Override
	public boolean isAnyStepAnnotated(Class<? extends Annotation> annotationClass) {
		checkNotNull(annotationClass, "null Class");
		Map<Step, StepImplementation> index = getStepIndex();

		boolean evaluation = false;
		Iterator<Map.Entry<Step, StepImplementation>> i = index.entrySet().iterator();
		while (!evaluation && i.hasNext()) {
			Map.Entry<Step, StepImplementation> mapEntry = i.next();
			StepImplementation implementation = mapEntry.getValue();
			Method method = implementation.getMethod();
			evaluation = null != method && method.isAnnotationPresent(annotationClass);
		}
		return evaluation;
	}

	@Override
	public String toString() {
		return getId();
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return null != obj && Martini.class.isInstance(obj) && Martini.class.cast(obj).getId().equals(getId());
	}

	protected static Builder builder() {
		return new Builder();
	}

	protected static class Builder {

		protected static final String MESSAGE_KEY = "martini.creation.exception";

		private Recipe recipe;
		private LinkedHashMap<Step, StepImplementation> index;

		protected Builder() {
			index = Maps.newLinkedHashMap();
		}

		protected Builder setRecipe(Recipe recipe) {
			this.recipe = recipe;
			return this;
		}

		protected Builder add(Step step, StepImplementation implementation) {
			index.put(step, implementation);
			return this;
		}

		protected DefaultMartini build() {
			ImmutableMap<Step, StepImplementation> immutableIndex = ImmutableMap.copyOf(index);
			ImmutableSet<MartiniTag> tags = getTags();
			return new DefaultMartini(recipe, immutableIndex, tags);
		}

		protected ImmutableSet<MartiniTag> getTags() {
			Pickle pickle = recipe.getPickle();
			List<PickleTag> pickleTags = pickle.getTags();

			List<DefaultMartiniTag> tags = pickleTags.stream()
				.map(this::getDefaultMartiniTag)
				.collect(Collectors.toList());
			return ImmutableSet.copyOf(tags);
		}

		protected DefaultMartiniTag getDefaultMartiniTag(PickleTag tag) {
			try {
				DefaultMartiniTag.Builder builder = DefaultMartiniTag.builder();
				return builder.setPickleTag(tag).build();
			}
			catch (Exception e) {
				MessageSource messageSource = MessageSources.getMessageSource(DefaultMartini.class);
				throw new MartiniException.Builder()
					.setCause(e)
					.setMessageSource(messageSource)
					.setKey(MESSAGE_KEY)
					.setArguments(recipe.getId())
					.build();
			}
		}
	}
}
