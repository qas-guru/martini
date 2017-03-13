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

package guru.qas.martini.gherkin;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

import static org.testng.Assert.assertEquals;

public class DefaultMixologyTest {

	@Test
	public void testMixology() throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		DefaultMixology factory = context.getBean(DefaultMixology.class);

		Resource resource = new ClassPathResource("/subsystem/sample.feature");
		Iterable<Recipe> recipes = factory.get(resource);

		int recipeCount = Iterables.size(recipes);
		assertEquals(1, recipeCount, "wrong number of Recipe objects returned");
	}
}
