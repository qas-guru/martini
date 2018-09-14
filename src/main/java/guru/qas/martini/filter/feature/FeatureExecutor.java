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

package guru.qas.martini.filter.feature;

import guru.qas.martini.Martini;
import guru.qas.martini.filter.id.AbstractIdentifierExecutor;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
public class FeatureExecutor extends AbstractIdentifierExecutor {

	@Override
	protected void assertValidArguments(Object... arguments) throws IllegalArgumentException {
		checkArgument(1 == arguments.length, "expected a single Feature name, found %s", arguments.length);
	}

	@Override
	protected String getIdentifier(Martini martini) {
		return martini.getFeatureName();
	}
}
