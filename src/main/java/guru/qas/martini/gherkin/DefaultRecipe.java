/*
Copyright 2018 Penny Rohr Curich

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

package guru.qas.martini.gherkin;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import gherkin.ast.Background;
import gherkin.ast.ScenarioDefinition;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class DefaultRecipe implements Recipe, Serializable {

	private static final long serialVersionUID = 1388547503104118707L;

	protected final FeatureWrapper featureWrapper;
	protected final Pickle pickle;
	protected final PickleLocation location;
	protected final ScenarioDefinition definition;

	public FeatureWrapper getFeatureWrapper() {
		return featureWrapper;
	}

	public Pickle getPickle() {
		return pickle;
	}

	public PickleLocation getLocation() {
		return location;
	}

	public ScenarioDefinition getScenarioDefinition() {
		return definition;
	}

	public DefaultRecipe(
		@Nonnull FeatureWrapper featureWrapper,
		@Nonnull Pickle pickle,
		@Nonnull PickleLocation location,
		@Nonnull ScenarioDefinition definition
	) {
		this.featureWrapper = checkNotNull(featureWrapper, "null FeatureWrapper");
		this.pickle = checkNotNull(pickle, "null Pickle");
		this.location = checkNotNull(location, "null Location");
		this.definition = checkNotNull(definition, "null ScenarioDefinition");
	}

	public Background getBackground() {
		List<ScenarioDefinition> children = featureWrapper.getChildren();
		List<Background> backgrounds = children.stream().filter(Background.class::isInstance).map(Background.class::cast).collect(Collectors.toList());
		checkState(backgrounds.isEmpty() || 1 == backgrounds.size(), "more than one Background identified");
		return backgrounds.isEmpty() ? null : backgrounds.get(0);
	}

	public String getId() {
		String featureName = getFeatureWrapper().getName();
		String scenarioName = getPickle().getName();
		int line = getLocation().getLine();
		String formatted = String.format("%s:%s:%s", featureName, scenarioName, line);
		return formatted.replaceAll("\\s+", "_");
	}
}
