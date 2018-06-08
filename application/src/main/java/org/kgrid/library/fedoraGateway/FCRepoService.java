package org.kgrid.library.fedoraGateway;

import com.complexible.common.openrdf.model.ModelIO;
import org.kgrid.library.exception.LibraryException;
import org.kgrid.library.fusekiGateway.NamespaceConstants;
import org.kgrid.library.model.ServerDetails;
import org.kgrid.library.services.FedoraConfiguration;
import org.apache.http.*;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.kgrid.shelf.domain.ArkId;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.impl.ContextStatement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.RDFFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class FCRepoService {

	private FedoraConfiguration fedoraConfiguration;

	private URI baseURI;

	private String userName;

	protected String password;

	private static final Logger logger = Logger.getLogger(FCRepoService.class);

	public FCRepoService(FedoraConfiguration fedoraConfiguration) {
		this.fedoraConfiguration = fedoraConfiguration;
		configureBaseURI();
	}

	public URI getBaseURI() {
		return baseURI;
	}

	// Transaction Handling:
	public URI createTransaction() throws LibraryException, URISyntaxException {

		URI transactionURI = new URI(baseURI + "fcr:tx/");

		HttpPost httpPost = new HttpPost(transactionURI);
		httpPost.addHeader(authenticate(httpPost));

		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpResponse response = httpClient.execute(httpPost) ;
			if(response.getStatusLine().getStatusCode() == HttpStatus.CREATED.value()) {
				transactionURI = new URI(response.getFirstHeader("Location").getValue());
			} else {
				String err = "Unable to create transaction.";
				logger.error(err);
				throw new LibraryException(err);
			}
		} catch (IOException e) {
			String err = "Exception occurred while creating transaction " + e.getMessage();
			logger.error(err);
			throw new LibraryException(err, e);
		}
		return transactionURI;
	}

	public void commitTransaction(URI transactionURI) throws LibraryException, URISyntaxException {
		URI transactionCommitURL = new URI(transactionURI + "/fcr:tx/fcr:commit");

		HttpPost httpPost = new HttpPost(transactionCommitURL);
		httpPost.addHeader(authenticate(httpPost));

		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpResponse response = httpClient.execute(httpPost) ;
			if(response.getStatusLine().getStatusCode() == HttpStatus.NO_CONTENT.value()) {
				logger.info("Transaction " + transactionCommitURL + " committed");
			} else {
				String err = "Unable to commit transaction with Id " + transactionURI;
				logger.error(err);
				throw new LibraryException(err);
			}

		} catch (IOException e) {
			String err = "Exception occurred while committing the transaction with id " + transactionURI + ". " + e.getMessage();
			logger.error(err);
			throw new LibraryException(err, e);
		}
	}

	public void rollbackTransaction(URI transactionURI) throws LibraryException, URISyntaxException {
		URI transactionRollBackURL = new URI(transactionURI + "/fcr:tx/fcr:rollback");

		HttpPost httpPost = new HttpPost(transactionRollBackURL);
		httpPost.addHeader(authenticate(httpPost));

		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			HttpResponse response = httpClient.execute(httpPost) ;
			if(response.getStatusLine().getStatusCode() == HttpStatus.NO_CONTENT.value()) {
				logger.info("Transaction " + transactionURI + " rolled back.");
			} else {
				String err = "Unable to roll back transaction with transaction URI " + transactionURI;
				logger.error(err);
				throw new LibraryException(err);
			}

		} catch (IOException e) {
			String err = "Exception occurred while rolling back the transaction with transaction URI " + transactionURI + ". " + e.getMessage();
			logger.error(err);
			throw new LibraryException(err, e);
		}
	}

	//Get:
	public boolean checkIfObjectExists(URI objectURI) throws LibraryException {

		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpGet httpGetRequest = new HttpGet(objectURI);

		httpGetRequest.addHeader(authenticate(httpGetRequest));

		try {
			HttpResponse httpResponse = httpClient.execute(httpGetRequest);
			StatusLine statusLine = httpResponse.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.OK.value())
				return true;
			else if (statusLine.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
				return false;
			}
			else {
				String err = "Could not find object at URI " + objectURI + " got HTTP response " + statusLine;
				logger.warn(err);
				throw new LibraryException(err);
			}
		} catch (IOException e) {
			String err = "Exception occurred while verifying object id "+ objectURI +" "+ e.getMessage();
			logger.error(err);
			throw new LibraryException(err, e);
		}
	}

	public Model getRDFData(URI objectURI) throws LibraryException {
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpGet httpGetRequest = new HttpGet(objectURI);

		httpGetRequest.addHeader(authenticate(httpGetRequest));
		httpGetRequest.addHeader("Accept", "application/n-triples");

		try {
			HttpResponse httpResponse = httpClient.execute(httpGetRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
				HttpEntity entity = httpResponse.getEntity();
				return ModelIO.read(entity.getContent(), RDFFormat.NTRIPLES);
			}
		} catch (IOException e) {
			String err = "Exception occurred while verifying object id "+ objectURI +"."+ e.getMessage();
			logger.error(err);
			throw new LibraryException(err, e);
		}
		return null;
	}

	public String getObjectContent(String objectId, String dataStreamId) throws LibraryException {
		try {
			return getObjectContent(new URI(baseURI + objectId + "/" + dataStreamId + "/"));
		} catch (URISyntaxException e) {
			throw new LibraryException("Invalid object uri " + baseURI + objectId + "/" + dataStreamId + "/");
		}
	}

	public String getObjectContent(URI objectURI) throws LibraryException {
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpGet httpGetRequest = new HttpGet(objectURI);
		httpGetRequest.addHeader(authenticate(httpGetRequest));

		StringBuilder chunk = new StringBuilder();
		HttpResponse httpResponse;

		try {
			httpResponse = httpClient.execute(httpGetRequest);
			HttpEntity entity = httpResponse.getEntity();
			if(httpResponse.getStatusLine().getStatusCode() == 200) {

				byte[] buffer = new byte[4096];
				if (entity != null) {
					InputStream inputStream = entity.getContent();
					int bytesRead;
					BufferedInputStream bis = new BufferedInputStream(inputStream);

					while ((bytesRead = bis.read(buffer)) != -1) {
						// TODO: Get fedora to store things in a better character encoding and change this
						chunk.append(new String(buffer, 0, bytesRead, Charset.forName("ISO-8859-1")));
					}
				}
			} else {
				logger.error("Exception occurred while retrieving object content for object " + objectURI + ". Request status code is "+httpResponse.getStatusLine().getStatusCode());
				throw new LibraryException("\"Exception occurred while retrieving object content for object " + objectURI + ". Request status code is "+
						httpResponse.getStatusLine().getStatusCode());
			}

		} catch (IOException e) {
			logger.error("Exception occurred while retrieving object content for object " +objectURI  + e.getMessage());
			throw new LibraryException("Exception occurred while retrieving object content for object "+ objectURI + e.getMessage(), e);
		}
		return chunk.toString();
	}

	public List<URI> getChildrenURIs(URI containerURI) throws LibraryException, URISyntaxException {
		Model container = getRDFData(containerURI);

		ArrayList<URI> uris = new ArrayList<>();
		if(container != null && container.size() > 0) {
			for (Object item : container.toArray()) {
				ContextStatement statement = (ContextStatement) item;
				IRI iri = SimpleValueFactory.getInstance().createIRI(NamespaceConstants.CONTAINS);
				if (statement.getPredicate().equals(iri)) {
					uris.add(new URI(statement.getObject().stringValue()));
				}
			}
		}
		return uris;
	}

	public boolean ping() throws IOException, LibraryException {

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet httpGetRequest = new HttpGet(baseURI);
		httpGetRequest.addHeader(authenticate(httpGetRequest));

		StatusLine statusLine = httpClient.execute(httpGetRequest).getStatusLine();

		if (statusLine.getStatusCode() == HttpStatus.OK.value())
			return true;
		else
			throw new LibraryException(statusLine.toString());
	}

	//Post:
	public URI createContainerWithAutoGeneratedName(URI parent) throws LibraryException {

		URI containerLocation;

		HttpPost httpPost = new HttpPost(parent);

		httpPost.addHeader(authenticate(httpPost));

		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpResponse response = httpClient.execute(httpPost);
			if(response.getStatusLine().getStatusCode() == HttpStatus.CREATED.value()) {
				containerLocation = new URI(response.getFirstHeader("Location").getValue());

			} else {
				String err = "Unable to create child resource for parent " + parent;
				logger.error(err);
				throw new LibraryException(err);
			}
		} catch (IOException|URISyntaxException e) {
			String err = "Exception occurred while creating child resource for parent " + parent + " " + e.getMessage();
			logger.error(err);
			throw new LibraryException(err, e);
		}

		return containerLocation;
	}

	// Put:
	public void putBinary(String binary, ArkId objIdentifier, String type) throws LibraryException, URISyntaxException {
		putBinary(binary, new URI(baseURI + objIdentifier.getFedoraPath() + "/" + type));
	}

	public void putBinary(String binary, URI objectURI, String type) throws LibraryException, URISyntaxException {
		putBinary(binary, new URI(objectURI + "/" + type));
	}

	public void putBinary(String binary, URI objectURI) throws LibraryException {

		HttpPut httpPutRequestPayload = new HttpPut(objectURI);

		httpPutRequestPayload.addHeader(authenticate(httpPutRequestPayload));

		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			StringEntity entity = new StringEntity(binary != null ? binary : "");
			httpPutRequestPayload.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPutRequestPayload);

			if(response != null &&
					(response.getStatusLine().getStatusCode() == HttpStatus.CREATED.value() ||
							response.getStatusLine().getStatusCode() == HttpStatus.NO_CONTENT.value())) {
				logger.info("Binary added successfully in the Object " + httpPutRequestPayload.getURI());
			} else {
				String err = "Exception occurred while creating binary for object " + objectURI + ". HTTPResponse is " + response;
				logger.error(err);
				throw new LibraryException(err);
			}
		} catch (IOException e) {
			String errString = "Exception occurred while creating binary for object " + objectURI + ". " + e.getMessage();
			logger.error(errString);
			throw new LibraryException(errString, e);
		}
	}

	public URI createContainer(URI uri) throws LibraryException, URISyntaxException {
		return createContainer(uri, "");
	}

	public URI createContainer(URI uri, String objectID) throws LibraryException, URISyntaxException {
		HttpClient httpClient = HttpClientBuilder.create().build();
		URI objectURI;
		if(uri.toString().endsWith("/")) {
			objectURI = new URI(uri + objectID);
		} else {
			objectURI = new URI(uri + "/" + objectID);
		}
		HttpPut httpPutRequest = new HttpPut(objectURI);

		httpPutRequest.addHeader(authenticate(httpPutRequest));

		try {
			HttpResponse httpResponse = httpClient.execute(httpPutRequest);
			if (httpResponse != null
					&& httpResponse.getStatusLine().getStatusCode() == HttpStatus.CREATED.value()) {
				logger.info("Successfully added new container " + objectURI);
				return new URI(httpResponse.getFirstHeader("Location").getValue());
			} else {
				String err =
						"Error occurred while creating object " + objectURI + " HttpResponse is "
								+ httpResponse;
				logger.error(err);
				throw new LibraryException(err);

			}
		} catch (IOException e) {
			String err = "Exception occurred while creating the container " + objectURI + ". " + e
					.getMessage() + " caused by " + e.getCause();
			logger.error(err);
			throw new LibraryException(err, e);
		}
	}

	public URI putRDFData(Model data, URI uri) throws LibraryException {

		if(data.size() < 1) {
			return uri;
		}

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPut httpPutRequest = new HttpPut(uri + "/fcr:metadata");

		httpPutRequest.addHeader(authenticate(httpPutRequest));

		//This header lets us overwrite triples without providing data for every triple in the fedora object
		httpPutRequest.addHeader("Prefer", "handling=lenient; received=\"minimal\"");

		try {
			ContentType textTurtle = ContentType.create("application/n-triples", Charset.forName("UTF-8"));
			String dataStr = ModelIO.toString(data, RDFFormat.NTRIPLES);
			StringEntity serializedData = new StringEntity(dataStr, textTurtle);

			httpPutRequest.setEntity(serializedData);

			HttpResponse httpResponse = httpClient.execute(httpPutRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.CREATED.value() ||
					httpResponse.getStatusLine().getStatusCode() == HttpStatus.NO_CONTENT.value()) {
				logger.info("Successfully added new rdf data at " + uri +
						" HttpResponse is " + httpResponse);
				return uri;
			} else {
				String err = "HTTP Error: " + httpResponse.getStatusLine() + " " + httpResponse.getEntity() + " Error occurred while adding rdf data " + data + " at uri " + uri;
				logger.error(err);
				throw new LibraryException(err);
			}
		} catch (IOException e) {
			String err = "Exception occurred while adding RDF data " + data + " \n" + e;
			logger.error(err);
			throw new LibraryException(err, e);
		}
	}

	//Patch:
	@Deprecated
	public URI sendPatchRequestForUpdatingTriples(String data, URI objectURI) throws LibraryException {

		logger.info("The Object URI is " + objectURI);
		HttpPatch httpPatch = new HttpPatch(objectURI);

		httpPatch.addHeader(authenticate(httpPatch));
		httpPatch.addHeader("Content-Type", "application/sparql-update");

		HttpClient client = HttpClientBuilder.create().build();

		try {
			InputStream requestEntity = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

			httpPatch.setEntity(new InputStreamEntity(requestEntity));
			HttpResponse response = client.execute(httpPatch);

			if (response != null && (response.getStatusLine().getStatusCode() == HttpStatus.NO_CONTENT.value())){
				logger.info("Successfully added triples for object " + objectURI + " and query " + data);
				Header location =  response.getFirstHeader("Location");
				if(location != null)
					return new URI (location.getValue());
				else
					return null;
			} else {
				String err = "Exception occurred while adding properties (triples) for object " + objectURI + " and query " + data + ". Got http response " + response.getStatusLine();
				logger.error(err);
				throw new LibraryException(err);
			}
		} catch (IOException|URISyntaxException e) {
			String err = "Exception occurred while adding properties (triples) for object "
					+ baseURI + objectURI + " and query " + data + " " + e;
			logger.error(err);
			throw new LibraryException(err, e);
		}
	}

	//Delete:
	public void deleteFedoraResource(URI deleteResourceURI) throws LibraryException {
		try {
			HttpDelete httpDelete = new HttpDelete(deleteResourceURI);
			httpDelete.addHeader(authenticate(httpDelete));

			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpResponse response = httpClient.execute(httpDelete);
			if (response.getStatusLine().getStatusCode() == HttpStatus.GONE.value()
					|| response.getStatusLine().getStatusCode() == HttpStatus.NO_CONTENT.value()) {
				logger.info("Fedora resource " + deleteResourceURI + " deleted.");
			} else {
				String err = "Unable to delete fedora resource " + deleteResourceURI + " due to " + EntityUtils.toString(response.getEntity());
				logger.error(err);
				throw new LibraryException(err);
			}

		} catch (IOException e) {
			String err =
					"Exception occurred while deleting fedora resource with URI " + deleteResourceURI + ". "
							+ e.getMessage();
			logger.error(err);
			throw new LibraryException(err, e);
		}
	}

	private void configureBaseURI(){

		ServerDetails configuration = fedoraConfiguration.getFedoraServerConfiguration();
		if(configuration != null){
			try {
				baseURI = new URI(configuration.getUrl());
			} catch (URISyntaxException e) {
				logger.error("Fedora uri is not valid. Check your properties.");
			}
			userName = configuration.getUsername();
			password= configuration.getPassword();

		} else{
			baseURI = null;
			logger.warn("No base uri configured for the fedora server.");
		}
	}

	public Header authenticate(HttpRequest request) throws LibraryException {
		Header header;
		try {
			header = new BasicScheme(StandardCharsets.UTF_8).authenticate(
					new UsernamePasswordCredentials(userName, password), request, null);
		} catch (AuthenticationException e) {
			String err = "Exception occurred while trying to authenticate at uri " + request + ". " + e;
			logger.error(err);
			throw new LibraryException(err, e);
		}
		return header;
	}
}