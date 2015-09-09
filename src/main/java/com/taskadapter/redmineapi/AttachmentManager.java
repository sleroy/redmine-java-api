package com.taskadapter.redmineapi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.AttachmentFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.internal.CopyBytesHandler;
import com.taskadapter.redmineapi.internal.io.MarkedIOException;
import com.taskadapter.redmineapi.internal.io.MarkedInputStream;

/**
 * Works with Attachments (files).
 * <p>Obtain it via RedmineManager:
 * <pre>
       RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
       AttachmentManager attachmentManager = redmineManager.getAttachmentManager();
 * </pre>
 *
 * <p>Sample usage:
 * <pre>
 File file = ...
 attachmentManager.addAttachmentToIssue(issueId, file, ContentType.TEXT_PLAIN.getMimeType());
 * </pre>
 *
 * @see RedmineManager#getAttachmentManager()
 */
public class AttachmentManager {
	/**
	 * @param exception
	 *            exception to unwrap.
	 * @param tag
	 *            target tag.
	 */
	private static void unwrapException(final RedmineException exception, final String tag) throws IOException {
		Throwable e = exception;
		while (e != null) {
			if (e instanceof MarkedIOException) {
				final MarkedIOException marked = (MarkedIOException) e;
				if (tag.equals(marked.getTag())) {
					throw marked.getIOException();
				}
			}
			e = e.getCause();
		}
	}

	private final ITransport transport;


	AttachmentManager(final ITransport _transport) {
		transport = _transport;
	}

	/**
	 *
	 * @param issueId database ID of the Issue
	 * @param attachmentFile the file to upload
	 * @param contentType MIME type. depending on this parameter, the file will be recognized by the server as
	 *                    text or image or binary. see http://en.wikipedia.org/wiki/Internet_media_type for possible MIME types.
	 *                    sample value: ContentType.TEXT_PLAIN.getMimeType()
	 * @return the created attachment object.
	 */
	public Attachment addAttachmentToIssue(final Integer issueId, final File attachmentFile, final String contentType) throws RedmineException, IOException {
		final Attachment attach = uploadAttachment(contentType, attachmentFile);
		final Issue issue = IssueFactory.create(issueId);
		issue.addAttachment(attach);
		transport.updateObject(issue);
		return attach;
	}

	/**
	 * Downloads the content of an {@link com.taskadapter.redmineapi.bean.Attachment} from the Redmine server.
	 *
	 * @param issueAttachment the {@link com.taskadapter.redmineapi.bean.Attachment}
	 * @return the content of the attachment as a byte[] array
	 * @throws RedmineCommunicationException thrown in case the download fails
	 */
	public byte[] downloadAttachmentContent(final Attachment issueAttachment)
			throws RedmineException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		downloadAttachmentContent(issueAttachment, baos);
		try {
			baos.close();
		} catch (final IOException e) {
			throw new RedmineInternalError(e);
		}
		return baos.toByteArray();
	}

	public void downloadAttachmentContent(final Attachment issueAttachment,
			final OutputStream stream) throws RedmineException {
		transport.download(issueAttachment.getContentURL(),
				new CopyBytesHandler(stream));
	}

	/**
	 * Delivers an {@link com.taskadapter.redmineapi.bean.Attachment} by its ID.
	 *
	 * @param attachmentID the ID
	 * @return the {@link com.taskadapter.redmineapi.bean.Attachment}
	 * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
	 * @throws RedmineException        thrown in case something went wrong in Redmine
	 * @throws NotFoundException       thrown in case an object can not be found
	 */
	public Attachment getAttachmentById(final int attachmentID) throws RedmineException {
		return transport.getObject(Attachment.class, attachmentID);
	}

	/**
	 * Uploads an attachment.
	 *
	 * @param contentType
	 *            content type of the attachment.
	 * @param content
	 *            attachment content stream.
	 * @return attachment content.
	 * @throws RedmineException
	 *             if something goes wrong.
	 * @throws IOException
	 *             if input cannot be read.
	 */
	public Attachment uploadAttachment(final String contentType, final File content)
			throws RedmineException, IOException {
		final InputStream is = new FileInputStream(content);
		try {
			return uploadAttachment(content.getName(), contentType, is, content.length());
		} finally {
			is.close();
		}
	}

	/**
	 * Uploads an attachment.
	 *
	 * @param fileName
	 *            file name of the attachment.
	 * @param contentType
	 *            content type of the attachment.
	 * @param content
	 *            attachment content stream.
	 * @return attachment content.
	 * @throws RedmineException
	 *             if something goes wrong.
	 * @throws java.io.IOException
	 *             if input cannot be read.
	 */
	public Attachment uploadAttachment(final String fileName, final String contentType,
			final byte[] content) throws RedmineException, IOException {
		final InputStream is = new ByteArrayInputStream(content);
		try {
			return uploadAttachment(fileName, contentType, is, content.length);
		} finally {
			try {
				is.close();
			} catch (final IOException e) {
				throw new RedmineInternalError("Unexpected exception", e);
			}
		}
	}

	/**
	 * Uploads an attachment.
	 *
	 * @param fileName
	 *            file name of the attachment.
	 * @param contentType
	 *            content type of the attachment.
	 * @param content
	 *            attachment content stream.
	 * @param _size
	 * @return attachment content.
	 * @throws RedmineException
	 *             if something goes wrong.
	 * @throws IOException
	 *             if input cannot be read. This exception cannot be thrown yet
	 *             (I am not sure if http client can distinguish "network"
	 *             errors and local errors) but is will be good to distinguish
	 *             reading errors and transport errors.
	 */
	public Attachment uploadAttachment(final String fileName, final String contentType,
			final InputStream content,
			final long _size) throws RedmineException, IOException {
		final InputStream wrapper = new MarkedInputStream(content,
				"uploadStream");
		final String token;
		try {
			token = transport.upload(wrapper, _size);
			final Attachment result = AttachmentFactory.create();
			result.setToken(token);
			result.setContentType(contentType);
			result.setFileName(fileName);
			return result;
		} catch (final RedmineException e) {
			unwrapException(e, "uploadStream");
			throw e;
		}
	}

}
