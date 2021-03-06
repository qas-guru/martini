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

package guru.qas.martini.event;

import org.springframework.context.PayloadApplicationEvent;
import org.springframework.core.ResolvableType;

import guru.qas.martini.result.MartiniResult;

@SuppressWarnings("WeakerAccess")
public abstract class MartiniScenarioEvent extends PayloadApplicationEvent<MartiniResult> {

	public MartiniScenarioEvent(Object source, MartiniResult payload) {
		super(source, payload);
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClass(MartiniResult.class);
	}
}
