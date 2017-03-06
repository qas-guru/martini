package guru.qas.martini.parser;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.*;

public class DefaultFeatureResourceLoaderTest {

	private ApplicationContext context;
	private DefaultFeatureResourceLoader bean;

	@BeforeClass
	public void setUpClass() {
		context = new ClassPathXmlApplicationContext("/guru/martini/applicationContext.xml");
		bean = context.getBean(DefaultFeatureResourceLoader.class);
	}

	@AfterClass
	public void tearDownClass() {
		bean = null;
		context = null;
	}

	@Test
	public void testDefaultConfiguration() throws IOException {
		List<Resource> resources = bean.getResources();
		Stream<String> stream = resources.stream().map(new ResourceToFilenameFunction());
		assertThat(stream)
			.contains("test1.feature")
			.withFailMessage("expected to find test1.feature in resources");
	}

	private static final class ResourceToFilenameFunction implements Function<Resource, String> {

		@Override
		public String apply(Resource resource) {
			return null == resource ? null : resource.getFilename();
		}
	}
}
