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
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import gherkin.ast.Step;
import gherkin.pickles.Pickle;
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

	protected DefaultMartini(
		Recipe recipe,
		ImmutableMap<Step, StepImplementation> stepIndex,
		Iterable<MartiniTag> tags
	) {
		this.recipe = recipe;
		this.stepIndex = stepIndex;
		this.tags = ImmutableSet.copyOf(tags);
	}

	@Override
	public String getId() {
		return recipe.getId();
	}

	@Override
	public String toString() {
		return getId();
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
			ImmutableSet<MartiniTag> tags = getTags();
			return new DefaultMartini(recipe, immutableIndex, tags);
		}

		protected ImmutableSet<MartiniTag> getTags() {
			Pickle pickle = recipe.getPickle();
			List<PickleTag> pickleTags = pickle.getTags();

			int tagCount = pickleTags.size();
			Set<MartiniTag> tags = Sets.newHashSetWithExpectedSize(tagCount);
			MartiniTag.Builder builder = MartiniTag.builder();
			for (PickleTag pickleTag : pickleTags) {
				try {
					MartiniTag tag = builder.build(pickleTag);
					tags.add(tag);
				} catch (MartiniException e) {
					throw new RuntimeException("unable to create Martini for scenario " + recipe.getId(), e);
				}
			}
			return ImmutableSet.copyOf(tags);
		}
	}
}
