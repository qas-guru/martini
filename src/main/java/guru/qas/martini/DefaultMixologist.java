package guru.qas.martini;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import guru.qas.martini.gherkin.GherkinResourceLoader;
import guru.qas.martini.gherkin.Mixology;
import guru.qas.martini.gherkin.Recipe;
import guru.qas.martini.step.StepImplementation;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("WeakerAccess")
@Service
public class DefaultMixologist implements Mixologist, InitializingBean, ApplicationContextAware {

	protected final GherkinResourceLoader loader;
	protected final Mixology mixology;
	protected final boolean unimplementedStepsFatal;
	protected final AtomicReference<ImmutableList<Martini>> martinisReference;

	protected ApplicationContext context;
	protected ImmutableList<Recipe> recipes;

	@Autowired
	protected DefaultMixologist(
		GherkinResourceLoader loader,
		Mixology mixology,
		@Value("${unimplemented.steps.fatal:#{false}}") boolean missingStepFatal
	) {
		this.loader = loader;
		this.mixology = mixology;
		this.unimplementedStepsFatal = missingStepFatal;
		this.martinisReference = new AtomicReference<>();
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		recipes = null;
		martinisReference.set(null);
		Resource[] resources = loader.getFeatureResources();
		initialize(resources);
	}

	protected void initialize(Resource[] resources) throws IOException {
		ImmutableList.Builder<Recipe> builder = ImmutableList.builder();
		for (Resource resource : resources) {
			Iterable<Recipe> recipes = mixology.get(resource);
			builder.addAll(recipes);
		}
		this.recipes = builder.build();
	}

	public void doSomething() {
		ImmutableList<Martini> martinis = getMartinis();
		System.out.println("breakpoint");
	}

	protected ImmutableList<Martini> getMartinis() {
		synchronized (martinisReference) {
			ImmutableList<Martini> martinis = martinisReference.get();
			if (null == martinis) {
				Map<String, StepImplementation> index = context.getBeansOfType(StepImplementation.class);
				Collection<StepImplementation> implementations = index.values();

				ImmutableList.Builder<Martini> builder = ImmutableList.builder();
				for (Recipe recipe : recipes) {
					Feature feature = recipe.getFeature();
					List<ScenarioDefinition> scenarios = feature.getChildren();
					for (ScenarioDefinition scenario : scenarios) {
						List<gherkin.ast.Step> gherkinSteps = scenario.getSteps();
						for (gherkin.ast.Step gherkinStep : gherkinSteps) {
							Martini martini = getMartini(recipe, gherkinStep, implementations);
							builder.add(martini);
						}
					}
				}
				martinis = builder.build();
				martinisReference.set(martinis);
			}
			return martinis;
		}
	}

	private Martini getMartini(Recipe recipe, gherkin.ast.Step step, Collection<StepImplementation> implementations) {

		List<StepImplementation> matches = Lists.newArrayList();
		for (StepImplementation implementation : implementations) {
			if (implementation.isMatch(step)) {
				matches.add(implementation);
			}
		}

		int count = matches.size();
		checkState(count < 2, "ambigous step; %s matches found for step %s", step);
		checkState(!unimplementedStepsFatal || 1 == count, "no implementation found matching step %s", step);

		StepImplementation implementation = 0 == count ? null : matches.get(0);
		return new DefaultMartini(recipe, step, implementation);
	}
}
