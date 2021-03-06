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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gherkin.ast.Step;
import guru.qas.martini.gate.MartiniGate;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;
import guru.qas.martini.tag.MartiniTag;

public interface Martini extends Serializable {

	String getId();

	Recipe getRecipe();

	Map<Step, StepImplementation> getStepIndex();

	Collection<MartiniGate> getGates();

	Collection<MartiniTag> getTags();

	/**
	 * Convenience method.
	 *
	 * @return name stored in Recipe's Feature
	 */
	String getFeatureName();

	/**
	 * Convenience method.
	 *
	 * @return name stored in Recipe's Pickle
	 */
	String getScenarioName();

	/**
	 * Convenience method.
	 *
	 * @return line stored in Pickle's PickleLocation
	 */
	int getScenarioLine();

	<T extends Annotation> List<T> getStepAnnotations(Class<T> implementation);

	boolean isAnyStepAnnotated(Class<? extends Annotation> implementation);
}