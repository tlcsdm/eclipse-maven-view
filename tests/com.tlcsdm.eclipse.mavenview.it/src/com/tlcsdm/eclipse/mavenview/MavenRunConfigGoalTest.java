package com.tlcsdm.eclipse.mavenview;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MavenRunConfigGoalTest {

	@Parameters(name = "{1} ({0})")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{

				{new MavenRunConfig().phases(Phase.CLEAN), "clean"},

				{new MavenRunConfig().phases(Phase.INSTALL), "install"},

				{new MavenRunConfig().phases(Phase.CLEAN, Phase.DEPLOY), "clean deploy"},

				{new MavenRunConfig().phases(Phase.COMPILE, Phase.DEPLOY, Phase.INSTALL), "compile install deploy"},

				{new MavenRunConfig().phases(Phase.INSTALL, Phase.COMPILE, Phase.DEPLOY), "compile install deploy"},

		});
	}

	private final MavenRunConfig config;
	private final String goalString;

	public MavenRunConfigGoalTest(MavenRunConfig config, String goalString) {
		this.config = config;
		this.goalString = goalString;
	}

	@Test
	public void test() {
		Assert.assertEquals(this.goalString, this.config.toGoalString());
	}

}
