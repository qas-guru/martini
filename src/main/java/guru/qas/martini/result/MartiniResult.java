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

package guru.qas.martini.result;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import guru.qas.martini.Martini;
import guru.qas.martini.event.SuiteIdentifier;
import guru.qas.martini.event.Status;

public interface MartiniResult {

	UUID getId();

	SuiteIdentifier getSuiteIdentifier();

	Martini getMartini();

	String getThreadGroupName();

	String getThreadName();

	Optional<Status> getStatus();

	Optional<Long> getStartTimestamp();

	Optional<Long> getEndTimestamp();

	Optional<Long> getExecutionTimeMs();

	List<StepResult> getStepResults();

	Set<String> getCategorizations();

	Optional<Exception> getException();
}
