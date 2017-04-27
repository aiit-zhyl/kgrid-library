package org.uofm.ot.FedoraAccessLayer;

import static org.junit.Assert.*;

import java.net.URI;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.uofm.ot.exception.ObjectTellerException;
import org.uofm.ot.fedoraAccessLayer.FCRepoService;
import org.uofm.ot.services.FedoraConfiguration;

/**
 * Created by nggittle on 4/19/17.
 */
public class FCRepoServiceTest {

  private FCRepoService fos;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();
  FedoraConfiguration fconfig;

  @Before
  public void setUp() {
    fconfig = new FedoraConfigurationBuilder()
        .fcRepoUrl("https://dlhs-fedora.med.umich.edu/fcrepo/rest/")
        .fcrepoUsername("fedoraAdmin")
        .fcrepoPassword("secret321")
        .fusekiUrl("https://dlhs-fedora.med.umich.edu/fuseki/test/query")
        .fusekiPrefix("https://dlhs-fedora.med.umich.edu/fcrepo/rest/")
        .build();
    fos = new FCRepoService();
    fos.setFedoraConfiguration(fconfig);
  }

  @Test
  public void testCheckObjectWhenObjectExists() throws Exception {
    assertTrue(fos.checkIfObjectExists(new URI("99999-fk41265c2w")));
  }

  @Test
  public void testCheckObjectWhenObjectDoesntExist() throws Exception {
    assertFalse(fos.checkIfObjectExists(null));
  }

  @Test
  public void testGetNumberOfObjects() throws Exception {
    SelectBuilder builder = new SelectBuilder();
    //?s rdf:type ot:KnowledgeObject
    Query query = builder
        .addPrefix("ot", "http://uofm.org/objectteller/")
        .addVar("*")
        //.addWhere("?s", "?p", "?o")
        .addWhere("?s", RDF.type, "ot:KnowledgeObject")
        .build();

    QueryExecution execution = QueryExecutionFactory
        .sparqlService("https://dlhs-fedora.med.umich.edu/fuseki/test/query", query);
    ResultSet resultSet = execution.execSelect();
    int i = 0;
    while(resultSet.hasNext()) {
      QuerySolution binding = resultSet.nextSolution();
      i++;
    }
    assertTrue(i > 0);
  }

  @Test
  public void createTransactionID() throws Exception {
    assertNotNull(fos.createTransaction());
  }

  @Test
  public void commitCorrectTransaction() throws Exception {
    fos.commitTransaction(fos.createTransaction());
  }

  @Test
  public void commitIncorrectTransaction() throws Exception {
    expectedEx.expect(ObjectTellerException.class);
    fos.commitTransaction(null);
  }

  @Test
  public void rollbackTransaction() throws Exception {
    fos.rollbackTransaction(fos.createTransaction());
  }

  @Test
  public void rollbackIncorrectTransaction() throws Exception {
    expectedEx.expect(ObjectTellerException.class);
    fos.rollbackTransaction(null);
  }

  @Test
  public void testContainerCreationWithAutogeneratedName() throws Exception {
    URI uri = new URI("https://dlhs-fedora.med.umich.edu/fcrepo/rest");
    URI parentUri = fos.createContainer(uri, "test2");
    URI location = fos.createContainerWithAutoGeneratedName(parentUri);
    assertNotNull(location);
    fos.deleteFedoraResource(location);
    fos.deleteFedoraResource(parentUri);
    fos.deleteFedoraResource(new URI(parentUri + "/fcr:tombstone"));


  }

  @Test
  public void testContainerCreation() throws Exception {
    String uri = "test";
  }


}