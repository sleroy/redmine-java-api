package com.tocea.redmine.java.api;

import java.io.File;
import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.TransportConfiguration;
import com.taskadapter.redmineapi.bean.Attachment;

public class NewAttachmentIntegrationTest {
	@SuppressWarnings("boxing")
	@Test
	public void testAttachment() throws IOException, RedmineException {

		final HttpClientBuilder create = HttpClientBuilder.create();
		final CloseableHttpClient client = create.build();

		final RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth("http://zeus/redmine", "sleroy", "ensapono", TransportConfiguration.create(client, null));

		final Attachment issue = redmineManager.getAttachmentManager().addAttachmentToIssue(18501,
				new File("build.gradle"), "application/text");
		System.out.println(issue);
		redmineManager.close();

	}
}
