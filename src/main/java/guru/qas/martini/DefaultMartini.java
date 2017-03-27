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

package guru.qas.martini;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import gherkin.ast.Feature;
import gherkin.ast.Step;
import gherkin.ast.Tag;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import guru.qas.martini.gherkin.MartiniTag;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;

/**
 * Default implementation of a Martini.
 */
@SuppressWarnings("WeakerAccess")
public class DefaultMartini implements Martini {

	protected final Recipe recipe;
	protected final ImmutableMap<Step, StepImplementation> stepIndex;
	protected final AtomicReference<List<MartiniTag>> martiniTagsRef;

	@Override
	public Recipe getRecipe() {
		return recipe;
	}

	@Override
	public Map<Step, StepImplementation> getStepIndex() {
		return stepIndex;
	}

	protected DefaultMartini(Recipe recipe, ImmutableMap<Step, StepImplementation> stepIndex) {
		this.recipe = recipe;
		this.stepIndex = stepIndex;
		this.martiniTagsRef = new AtomicReference<>();
	}

	@Override
	public List<Tag> getFeatureTags() {
		Feature feature = recipe.getFeature();
		List<Tag> tags = feature.getTags();
		return ImmutableList.copyOf(tags);
	}

	@Override
	public List<PickleTag> getScenarioTags() {
		Pickle pickle = recipe.getPickle();
		return pickle.getTags();
	}

	@Override
	public List<MartiniTag> getTags() {
		List<MartiniTag> martiniTags;
		synchronized(martiniTagsRef) {
			martiniTags = martiniTagsRef.get();
			if (null == martiniTags) {
				martiniTags = Lists.newArrayList();
				List<Tag> featureTags = getFeatureTags();
				for (Tag featureTag : featureTags) {
					MartiniTag tag = MartiniTag.builder().build(featureTag);
					martiniTags.add(tag);
				}
				for (PickleTag pickleTag : getScenarioTags()) {
					MartiniTag tag = MartiniTag.builder().build(pickleTag);
					martiniTags.add(tag);
				}
			}
			martiniTagsRef.set(martiniTags);
		}
		return martiniTags;
	}

	@Override
	public String toString() {
		Feature feature = recipe.getFeature();
		Pickle pickle = recipe.getPickle();
		PickleLocation location = pickle.getLocations().get(0);
		return String.format("Feature: %s\nResource: %s\nScenario: %s\nLine: %s",
			feature.getName(),
			recipe.getSource(),
			pickle.getName(),
			location.getLine());
	}

	protected static Builder builder() {
		return new Builder();
	}

	protected static class Builder {

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
			return new DefaultMartini(recipe, immutableIndex);
		}
	}
}
