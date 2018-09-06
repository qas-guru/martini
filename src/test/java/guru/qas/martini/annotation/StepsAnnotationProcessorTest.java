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

package guru.qas.martini.annotation;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import guru.qas.martini.spring.StepsAnnotationProcessor;
import guru.qas.martini.step.StepImplementation;
import nonfixture.DuplicateGivenBeanA;
import nonfixture.DuplicateGivenBeanB;
import fixture.TestSteps;
import nonfixture.MultipleGivenBean;
import nonfixture.PrivateGivenMethodBean;

import static org.testng.Assert.*;

public class StepsAnnotationProcessorTest {

	@Test
	public void testPostProcessAfterInitialization() throws NoSuchMethodException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		TestSteps steps = context.getBean(TestSteps.class);

		Class<?> wrapped = AopUtils.getTargetClass(steps);
		Method method = wrapped.getMethod("anotherStep", String.class);

		Map<String, StepImplementation> givenBeanIndex = context.getBeansOfType(StepImplementation.class);
		Collection<StepImplementation> givens = givenBeanIndex.values();

		List<StepImplementation> matches = Lists.newArrayList();
		for (StepImplementation given : givens) {
			Method givenMethod = given.getMethod();
			if (givenMethod.equals(method)) {
				matches.add(given);
			}
		}

		int count = matches.size();
		assertEquals(count, 1, "wrong number of GivenStep objects registered for TestSteps.anotherStep()");

		StepImplementation match = matches.get(0);
		Pattern pattern = match.getPattern();
		Matcher matcher = pattern.matcher("another \"(.+)\" here");
		assertTrue(matcher.find(), "expected Pattern to match Gherkin regular expression");
		assertEquals(matcher.groupCount(), 1, "wrong number of parameters captured for TestSteps.anotherStep()");
	}

	@Test(expectedExceptions = FatalBeanException.class)
	public void testDuplicateStep() {
		DuplicateGivenBeanA beanA = new DuplicateGivenBeanA();
		DuplicateGivenBeanB beanB = new DuplicateGivenBeanB();
		ClassPathXmlApplicationContext context = getContext(beanA, beanB);

		process(context, beanA);
		process(context, beanB);
	}

	private static ClassPathXmlApplicationContext getContext(Object... beans) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("emptyContext.xml");
		ConfigurableListableBeanFactory factory = context.getBeanFactory();
		for (Object bean : beans) {
			factory.registerSingleton(bean.getClass().getName(), bean);
		}
		return context;
	}

	private static void process(ClassPathXmlApplicationContext context, Object... beans) {
		StepsAnnotationProcessor processor = context.getBean(StepsAnnotationProcessor.class);
		for (Object bean : beans) {
			processor.postProcessAfterInitialization(bean, bean.getClass().getName());
		}
	}

	@Test
	public void testMultipleGivenRegex() throws NoSuchMethodException {
		MultipleGivenBean source = new MultipleGivenBean();
		ClassPathXmlApplicationContext context = getContext(source);
		process(context, source);

		MultipleGivenBean steps = context.getBean(MultipleGivenBean.class);
		Class<?> wrapped = AopUtils.getTargetClass(steps);
		Method method = wrapped.getMethod("doSomething");

		Map<String, StepImplementation> givenBeanIndex = context.getBeansOfType(StepImplementation.class);
		Collection<StepImplementation> givens = givenBeanIndex.values();

		Set<String> matches = Sets.newHashSetWithExpectedSize(2);
		for (StepImplementation given : givens) {
			Method givenMethod = given.getMethod();
			if (givenMethod.equals(method)) {
				Pattern pattern = given.getPattern();
				String regex = pattern.pattern();
				matches.add(regex);
			}
		}

		int count = matches.size();
		assertEquals(count, 2, "wrong number of GivenStep objects registered for MultipleGivenBean.getMartinis()");

		Set<String> expected = Sets.newHashSet(
			"this is regular expression one", "this is regular expression two");
		assertEquals(matches, expected, "Steps contain wrong regex Pattern objects");
	}

	@Test(expectedExceptions = FatalBeanException.class)
	public void testInaccessibleMethod() {
		PrivateGivenMethodBean bean = new PrivateGivenMethodBean();
		ClassPathXmlApplicationContext context = getContext(bean);
		process(context, bean);
	}

	@Test(expectedExceptions = FatalBeanException.class)
	public void testNonSingletonSteps() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("emptyContext.xml");
		ConfigurableListableBeanFactory factory = context.getBeanFactory();

		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(DuplicateGivenBeanA.class);
		definition.setLazyInit(false);
		definition.setScope("prototype");

		BeanDefinitionRegistry registry = BeanDefinitionRegistry.class.cast(factory);
		registry.registerBeanDefinition(DuplicateGivenBeanA.class.getName(), definition);
		process(context, new DuplicateGivenBeanA());
	}
}
