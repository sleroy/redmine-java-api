package com.taskadapter.redmineapi.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Redmine's Issue.
 * <p>
 * Note that methods returning lists of elements (like getRelations(), getWatchers(), etc return
 * unmodifiable collections.
 * You need to use methods like addRelations() if you want to add elements, e.g.:
 * <pre>
 *     issue.addRelations(Collections.singletonList(relation));
 * </pre>
 */
public class Issue implements Identifiable {

    /**
     * database ID.
     */
    private final Integer id;

    private String subject;
    private Integer parentId;
    private Float estimatedHours;
    private Float spentHours;
    private User assignee;
    private String priorityText;
    private Integer priorityId;
    private Integer doneRatio;
    private Project project;
    private User author;
    private Date startDate;
    private Date dueDate;
    private Tracker tracker;
    private String description;
    private Date createdOn;
    private Date updatedOn;
    private Integer statusId;
    private String statusName;
    private Version targetVersion;
    private IssueCategory category;

    /**
     * Some comment describing the issue update
     */
    private String notes;

    /**
     * can't have two custom fields with the same ID in the collection, that's why it is declared
     * as a Set, not a List.
     */
    private final Set<CustomField> customFields = new HashSet<CustomField>();
    private final Set<Journal> journals = new HashSet<Journal>();
    private final Set<IssueRelation> relations = new HashSet<IssueRelation>();
    private final Set<Attachment> attachments = new HashSet<Attachment>();
    private final Set<Changeset> changesets = new HashSet<Changeset>();
    private final Set<Watcher> watchers = new HashSet<Watcher>();

    public Issue() {
        id = null;
    }

    /**
     * @param id database ID.
     */
    Issue(final Integer id) {
        this.id = id;
    }

    public void addAttachment(final Attachment attachment) {
        attachments.add(attachment);
    }

    public void addAttachments(final Collection<Attachment> collection) {
        attachments.addAll(collection);
    }

    public void addChangesets(final Collection<Changeset> changesets) {
        this.changesets.addAll(changesets);
    }

    /**
     * If there is a custom field with the same ID already present in the Issue,
     * the new field replaces the old one.
     *
     * @param customField the field to add to the issue.
     */
    public void addCustomField(final CustomField customField) {
        customFields.add(customField);
    }

    /**
     * NOTE: The custom field(s) <strong>must have correct database ID set</strong> to be saved to Redmine. This is Redmine REST API's limitation.
     */
    public void addCustomFields(final Collection<CustomField> customFields) {
        this.customFields.addAll(customFields);
    }

    public void addJournals(final Collection<Journal> journals) {
        this.journals.addAll(journals);
    }

    public void addRelations(final Collection<IssueRelation> collection) {
        relations.addAll(collection);
    }

    public void addWatchers(final Collection<Watcher> watchers) {
        this.watchers.addAll(watchers);
    }

    public void clearCustomFields() {
        customFields.clear();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
			return true;
		}
        if (o == null || getClass() != o.getClass()) {
			return false;
		}

        final Issue issue = (Issue) o;

        if (id != null ? !id.equals(issue.id) : issue.id != null) {
			return false;
		}

        return true;
    }

    public User getAssignee() {
        return assignee;
    }

    /**
     * Don't forget to use <i>Include.attachments</i> flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.attachments);
     * </pre>
     * @return unmodifiable collection of entries or empty collection if no objects found.
     * @see com.taskadapter.redmineapi.Include#attachments
     */
    public Collection<Attachment> getAttachments() {
        return Collections.unmodifiableCollection(attachments);
    }

  public User getAuthor() {
    return author;
}

    public IssueCategory getCategory() {
        return category;
    }

    /**
     * Don't forget to use Include.changesets flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.changesets);
     * </pre>
     * @return unmodifiable collection of entries or empty collection if no objects found.
     * @see com.taskadapter.redmineapi.Include#changesets
     */
    public Collection<Changeset> getChangesets() {
        return Collections.unmodifiableCollection(changesets);
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * Deprecated. Please use the new getCustomFieldByName() method instead because the return value of this method
     * is not consistent with getCustomFieldById().
     *
     * @return the value or NULL if the field is not found
     *
     * @see #getCustomFieldByName(String customFieldName)
     */
    @Deprecated
    public String getCustomField(final String fieldName) {
        for (final CustomField f : customFields) {
            if (f.getName().equals(fieldName)) {
                return f.getValue();
            }
        }
        return null;
    }

    /**
     * @return the custom field with given Id or NULL if the field is not found
     */
    public CustomField getCustomFieldById(final int customFieldId) {
        if(customFields == null) {
			return null;
		}
        for (final CustomField customField : customFields) {
            if (customFieldId == customField.getId()) {
                return customField;
            }
        }
        return null;
    }

    /**
     * @return the custom field with given name or NULL if the field is not found
     */
    public CustomField getCustomFieldByName(final String customFieldName) {
        if(customFields == null) {
			return null;
		}
        for (final CustomField customField : customFields) {
            if (customFieldName.equals(customField.getName())) {
                return customField;
            }
        }
        return null;
    }

    /**
     * @return unmodifiable collection of Custom Field objects. the collection may be empty, but it is never NULL.
     */
    public Collection<CustomField> getCustomFields() {
        return Collections.unmodifiableCollection(customFields);
    }

    /**
     * Description is empty by default, not NULL.
     */
    public String getDescription() {
        return description;
    }

    public Integer getDoneRatio() {
        return doneRatio;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Float getEstimatedHours() {
        return estimatedHours;
    }

    @Override
    /**
     * @return id. can be NULL for Issues not added to Redmine yet
     */
    public Integer getId() {
        return id;
    }

    /**
     * Don't forget to use Include.journals flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.journals);
     * </pre>
     * @return unmodifiable collection of Journal entries or empty collection if no objects found. Never NULL.
     * @see com.taskadapter.redmineapi.Include#journals
     */
    public Collection<Journal> getJournals() {
        return Collections.unmodifiableCollection(journals);
    }

    public String getNotes() {
        return notes;
    }

    /**
	     * Parent Issue ID, or NULL for issues without a parent.
	     *
	     * @return NULL, if there's no parent
	     */
	    public Integer getParentId() {
	        return parentId;
	    }

    public Integer getPriorityId() {
        return priorityId;
    }

    public String getPriorityText() {
        return priorityText;
    }

    public Project getProject() {
        return project;
    }

    /**
     * Relations are only loaded if you include Include.relations when loading the Issue.
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.relations);
     * </pre>
     * <p>Since the returned collection is not modifiable, you need to use addRelations() method
     * if you want to add elements, e.g.:
     * <pre>
     *     issue.addRelations(Collections.singletonList(relation));
     * </pre>
     * @return unmodifiable collection of Relations or EMPTY collection if none found. Never returns NULL.
     * @see com.taskadapter.redmineapi.Include#relations
     */
    public Collection<IssueRelation> getRelations() {
        return Collections.unmodifiableCollection(relations);
    }

    public Float getSpentHours() {
        return spentHours;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public String getSubject() {
        return subject;
    }

    public Version getTargetVersion() {
        return targetVersion;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Don't forget to use Include.watchers flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.watchers);
     * </pre>
     * @return unmodifiable collection of entries or empty collection if no objects found.
     * @see com.taskadapter.redmineapi.Include#watchers
     */
    public Collection<Watcher> getWatchers() {
        return Collections.unmodifiableCollection(watchers);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public void setAssignee(final User assignee) {
        this.assignee = assignee;
    }

    public void setAuthor(final User author) {
        this.author = author;
    }

    public void setCategory(final IssueCategory category) {
        this.category = category;
    }

    public void setCreatedOn(final Date createdOn) {
        this.createdOn = createdOn;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setDoneRatio(final Integer doneRatio) {
        this.doneRatio = doneRatio;
    }

    public void setDueDate(final Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setEstimatedHours(final Float estimatedTime) {
        estimatedHours = estimatedTime;
    }

    /**
     * @param notes Some comment describing the issue update
     */
    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public void setParentId(final Integer parentId) {
        this.parentId = parentId;
    }


    public void setPriorityId(final Integer priorityId) {
        this.priorityId = priorityId;
    }

    /**
     * @deprecated This method has no effect when creating issues on Redmine Server, so we might as well just delete it
     * in the future releases.
     */
    @Deprecated
	public void setPriorityText(final String priority) {
        priorityText = priority;
    }

    public void setProject(final Project project) {
        this.project = project;
    }

    public void setSpentHours(final Float spentHours) {
         this.spentHours = spentHours;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public void setStatusId(final Integer statusId) {
        this.statusId = statusId;
    }

    public void setStatusName(final String statusName) {
        this.statusName = statusName;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public void setTargetVersion(final Version version) {
        targetVersion = version;
    }

    public void setTracker(final Tracker tracker) {
        this.tracker = tracker;
    }

    public void setUpdatedOn(final Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        return "Issue [id=" + id + ", subject=" + subject + "]";
    }
}
