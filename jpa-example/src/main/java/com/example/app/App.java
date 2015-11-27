package com.example.app;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.example.app.entity.Geek;
import com.example.app.entity.IdCard;
import com.example.app.entity.Period;
import com.example.app.entity.Person;
import com.example.app.entity.Phone;
import com.example.app.entity.Project;

public class App {

	private static final Logger LOGGER = Logger.getLogger("JPA");

	private EntityManagerFactory factory;
	private EntityManager entityManager;
	private EntityTransaction transaction;

	public static void main(String[] args) {
		new App().run();
	}

	public void run() {
		try {
			factory = Persistence.createEntityManagerFactory("PersistenceUnit");
			entityManager = factory.createEntityManager();
			persistPerson();
			persistGeek();
			loadPersons();
			addPhones();
			createProject();
			queryProject();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			e.printStackTrace();
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
			if (factory != null) {
				factory.close();
			}
		}
	}

	private void queryProject() {
		TypedQuery<Project> query = entityManager.createQuery(
				"from Project p where p.projectPeriod.startDate = :startDate",
				Project.class)
				.setParameter("startDate", createDate(1, 1, 2015));
		List<Project> resultList = query.getResultList();
		for (Project project : resultList) {
			LOGGER.info(project.getProjectPeriod().getStartDate().toString());
		}
	}

	private void createProject() {
		List<Geek> resultList = entityManager
				.createQuery(
						"from Geek where favouriteProgrammingLanguage = :fpl",
						Geek.class).setParameter("fpl", "Java").getResultList();
		transaction = entityManager.getTransaction();
		transaction.begin();

		Project project = new Project();
		project.setTitle("Java Project");
		project.setProjectType(Project.ProjectType.TIME_AND_MATERIAL);

		Period period = new Period();
		period.setStartDate(createDate(1, 1, 2015));
		period.setEndDate(createDate(31, 12, 2015));
		project.setProjectPeriod(period);
		for (Geek geek : resultList) {
			project.getGeeks().add(geek);
			geek.getProjects().add(project);
		}
		entityManager.persist(project);
		transaction.commit();
	}

	private Date createDate(int day, int month, int year) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.DAY_OF_MONTH, day);
		gc.set(Calendar.MONTH, month - 1);
		gc.set(Calendar.YEAR, year);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new Date(gc.getTimeInMillis());
	}

	private void addPhones() {

		transaction = entityManager.getTransaction();
		transaction.begin();

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Person> query = builder.createQuery(Person.class);
		Root<Person> personRoot = query.from(Person.class);
		query.where(builder.and(
				builder.equal(personRoot.get("firstName"), "Homer"),
				builder.equal(personRoot.get("lastName"), "Simpson")));
		List<Person> resultList = entityManager.createQuery(query)
				.getResultList();

		for (Person person : resultList) {
			Phone phone = new Phone();
			phone.setNumber("+49 1234 456789");
			entityManager.persist(phone);
			person.getPhones().add(phone);
			phone.setPerson(person);
		}

		transaction.commit();
	}

	private void persistPerson() {

		transaction = entityManager.getTransaction();

		try {
			transaction.begin();

			Person person = new Person();
			person.setFirstName("Homer");
			person.setLastName("Simpson");
			entityManager.persist(person);

			IdCard idCard = new IdCard();
			idCard.setIdNumber("4711");
			idCard.setIssueDate(new Date());
			person.setIdCard(idCard);
			entityManager.persist(idCard);
			transaction.commit();
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private void persistGeek() {

		transaction = entityManager.getTransaction();
		transaction.begin();

		Geek geek = new Geek();
		geek.setFirstName("Gavin");
		geek.setLastName("Coffee");
		geek.setFavouriteProgrammingLanguage("Java");
		entityManager.persist(geek);
		
		geek = new Geek();
		geek.setFirstName("Thomas");
		geek.setLastName("Micro");
		geek.setFavouriteProgrammingLanguage("C#");
		entityManager.persist(geek);
		
		geek = new Geek();
		geek.setFirstName("Christian");
		geek.setLastName("Cup");
		geek.setFavouriteProgrammingLanguage("Java");
		entityManager.persist(geek);
		transaction.commit();
	}

	private void loadPersons() {
		
		entityManager.clear();
		
		TypedQuery<Person> query = entityManager.createQuery(
				"from Person p left join fetch p.phones", Person.class);
		List<Person> resultList = query.getResultList();
		
		for (Person person : resultList) {
			StringBuilder sb = new StringBuilder();
			sb.append(person.getFirstName()).append(" ")
					.append(person.getLastName());
			if (person instanceof Geek) {
				Geek geek = (Geek) person;
				sb.append(" ").append(geek.getFavouriteProgrammingLanguage());
			}
			IdCard idCard = person.getIdCard();
			if (idCard != null) {
				sb.append(" ").append(idCard.getIdNumber()).append(" ")
						.append(idCard.getIssueDate());
			}
			List<Phone> phones = person.getPhones();
			for (Phone phone : phones) {
				sb.append(" ").append(phone.getNumber());
			}
			LOGGER.info(sb.toString());
		}
	}
}
