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

package guru.qas.martini.scope;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.stereotype.Component;

@SuppressWarnings("WeakerAccess")
@Component
public class ScenarioScope implements Scope {

	private static final InheritableThreadLocal<Map<String, Object>> SCOPE_REF = new InheritableThreadLocal<Map<String, Object>>() {
		@Override
		protected Map<String, Object> initialValue() {
			return new HashMap<>();
		}
	};

	private static final InheritableThreadLocal<String> CONVO_REF = new InheritableThreadLocal<>();

	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Map<String, Object> scope = SCOPE_REF.get();
		return scope.computeIfAbsent(name, k -> objectFactory.getObject());
	}

	@Override
	public Object remove(String name) {
		Map<String, Object> scope = SCOPE_REF.get();
		return scope.remove(name);
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {

	}

	@Override
	public Object resolveContextualObject(String key) {
		return null;
	}

	public void setConversationId(String conversationId) {
		CONVO_REF.set(conversationId);
	}

	@Override
	public String getConversationId() {
		return CONVO_REF.get();
	}

	public void clear() {
		Map<String, Object> scope = SCOPE_REF.get();
		scope.clear();
		CONVO_REF.remove();
	}
}
