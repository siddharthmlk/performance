package victor.training.jpa.perf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class NPlusOneTest {

	private static final Logger log = LoggerFactory.getLogger(NPlusOneTest.class);

	@Autowired
	private EntityManager em;

	@Before
	public void persistData() {
		em.persist(new Parent("Victor")
				.addChild(new Child("Emma"))
				.addChild(new Child("Vlad"))
		);
		em.persist(new Parent("Peter")
				.addChild(new Child("Maria"))
				.addChild(new Child("Stephan"))
				.addChild(new Child("Paul"))
		);
		TestTransaction.end();
		TestTransaction.start();
	}


	@Test
	public void nPlusOne() {
		List<Parent> parents = em.createQuery("SELECT  p FROM Parent p LEFT JOIN FETCH p.children ", Parent.class)/*.setMaxResults(3)*/.getResultList(); //PageRequest sau

		System.out.println(parents.get(0) == parents.get(1));
		Parent parent = em.find(Parent.class, 1l);
		System.out.println(parent == parents.get(1));

		// 500 linii mai tarziu
		int totalChildren = countChildren(new HashSet<>(parents));
		assertThat(totalChildren).isEqualTo(5);
	}

	private int countChildren(Collection<Parent> parents) {
		log.debug("Start iterating over {} parents: {}", parents.size(), parents);

		int total = 0;
		for (Parent parent : parents) {
			Set<Child> children = parent.getChildren();
			log.debug("Adica eu chem .size() si ala imi face query !?!" +
				" WTF? Oare pe ce chem size() : " + children.getClass());
			total += children.size();
		}
		log.debug("Done counting: {} children", total);
		return total;
	}

}
