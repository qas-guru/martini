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

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

/**
 * The main Martini core endpoint from which Martini instances may be obtained.
 */
public interface Mixologist {

	String IMPLEMENTATION_KEY = "martini.mixologist.implementation";

	/**
	 * @return all available Martini instances
	 */
	Collection<Martini> getMartinis();

	/**
	 * @param spelFilter Spring SPeL expression, e.g. "isSmoke()"
	 * @return all matching Martini instances
	 */
	Collection<Martini> getMartinis(@Nullable String spelFilter);
}
