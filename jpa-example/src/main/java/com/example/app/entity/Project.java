package com.example.app.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "T_PROJECT")
public class Project {

	private Long id;

	private String title;

	private List<Geek> geeks = new ArrayList<Geek>();

	private ProjectType projectType;

	private Period projectPeriod;

	private List<Period> billingPeriods = new ArrayList<Period>();

	public enum ProjectType {
		FIXED, TIME_AND_MATERIAL
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_PROJECT")
	@SequenceGenerator(name = "S_PROJECT", sequenceName = "S_PROJECT", allocationSize = 100)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "TITLE")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@ManyToMany
	@JoinTable(name = "T_GEEK_PROJECT", joinColumns = { @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "GEEK_ID", referencedColumnName = "ID") })
	public List<Geek> getGeeks() {
		return geeks;
	}

	public void setGeeks(List<Geek> geeks) {
		this.geeks = geeks;
	}

	@Enumerated(EnumType.STRING)
	public ProjectType getProjectType() {
		return projectType;
	}

	public void setProjectType(ProjectType projectType) {
		this.projectType = projectType;
	}

	@Embedded
	public Period getProjectPeriod() {
		return projectPeriod;
	}

	public void setProjectPeriod(Period projectPeriod) {
		this.projectPeriod = projectPeriod;
	}

	@ElementCollection
	@CollectionTable(name = "T_BILLING_PERIOD", joinColumns = @JoinColumn(name = "PROJECT_ID"))
	public List<Period> getBillingPeriods() {
		return billingPeriods;
	}

	public void setBillingPeriods(List<Period> billingPeriods) {
		this.billingPeriods = billingPeriods;
	}
}
