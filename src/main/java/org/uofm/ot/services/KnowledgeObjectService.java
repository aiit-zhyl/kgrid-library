package org.uofm.ot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uofm.ot.exception.ObjectTellerException;
import org.uofm.ot.fedoraAccessLayer.*;
import org.uofm.ot.fusekiAccessLayer.FusekiService;
import org.uofm.ot.knowledgeObject.Citation;
import org.uofm.ot.knowledgeObject.FedoraObject;
import org.uofm.ot.knowledgeObject.Metadata;
import org.uofm.ot.knowledgeObject.Payload;
import org.uofm.ot.model.User;

import java.util.ArrayList;
import java.util.List;


@Service
public class KnowledgeObjectService {
	
	@Autowired
	private FusekiService fusekiService;
	
	@Autowired
	private GetFedoraObjectService getFedoraObjectService;
	
	@Autowired
	private EditFedoraObjectService editFedoraObjectService;
	
	@Autowired
	private DeleteFedoraResourceService deleteFedoraResourceService;
	
	@Autowired
	private CreateFedoraObjectService createFedoraObjectService;
	
	public FedoraObject getKnowledgeObject(String uri) throws ObjectTellerException {

		FedoraObject object = fusekiService.getKnowledgeObject(uri);
		if(object != null) {
			List<Citation> citations = fusekiService.getObjectCitations(uri);	
			object.getMetadata().setCitations(citations);
		}
		return object;
	}
	
	public FedoraObject editObject(FedoraObject newObject,String objectURI) throws ObjectTellerException {
		addOrEditMetadata(objectURI, newObject.getMetadata());
		editPayload(objectURI, newObject.getPayload());
		editInputMessageContent(objectURI, newObject.getInputMessage());
		editOutputMessageContent(objectURI, newObject.getInputMessage());
		FedoraObject updatedObject = getCompleteKnowledgeObject(objectURI);
		return updatedObject ; 
	}
	
	public void deleteObject(String uri) throws ObjectTellerException {
		deleteFedoraResourceService.deleteObject(uri);
	}
	
	public List<FedoraObject> getKnowledgeObjects(boolean published) throws ObjectTellerException {
		List<FedoraObject> fedoraObjects = null;
		fedoraObjects = fusekiService.getFedoraObjects(published);
		return fedoraObjects;
	}
	
	public Integer getNumberOfPublishedObjects() throws ObjectTellerException {
		return fusekiService.getNumberOfPublishedObjects();
	}
	
	public FedoraObject getCompleteKnowledgeObject(String uri) throws ObjectTellerException {
		
		FedoraObject object = getKnowledgeObject(uri);	
		
		Payload payload = fusekiService.getPayloadProperties(uri);
		
		payload.setContent(getPayloadContent(uri));
		
		object.setPayload(payload);

		object.setLogData(getProvData(uri));

		object.setInputMessage(getInputMessageContent(uri));

		object.setOutputMessage(getOutputMessageContent(uri));
		
		return object;
	}
	
	public Metadata addOrEditMetadata(String uri, Metadata newMetadata) throws ObjectTellerException {
	
		List<Citation> oldCitations = fusekiService.getObjectCitations(uri);
		
		editFedoraObjectService.editObjectMetadata(newMetadata,uri);
		
		
		if(newMetadata != null && newMetadata.getCitations() != null) {
			List<Citation> editCitations = new ArrayList<Citation>();

			boolean firstCitation = true ; 
			// To add new citations
			for (Citation citation : newMetadata.getCitations()) {
				if(citation.getCitation_id() == null && citation.getCitation_title() != null && citation.getCitation_at() != null ) {

					if(firstCitation) {
						boolean citationParentExist = createFedoraObjectService.checkIfObjectExists(uri+"/"+ChildType.CITATIONS.getChildType());

						if(!citationParentExist) 
							createFedoraObjectService.createContainer(uri+"/"+ChildType.CITATIONS.getChildType(), null);

						firstCitation = false ;

					}
					
					String location = createFedoraObjectService.createContainerWithAutoGeneratedName(uri+"/"+ChildType.CITATIONS.getChildType(), null);

					citation.setCitation_id(location);

					createFedoraObjectService.addCitationProperties(citation, uri+"/"+ChildType.CITATIONS.getChildType()+"/"+location, null);
				} else {
					if(citation.getCitation_id() != null )
						editCitations.add(citation);
				}
			}
			
			editFedoraObjectService.editCitations(uri,editCitations);
			oldCitations.removeAll(editCitations);
		}
		
		if(oldCitations.isEmpty() == false){
			for (Citation citation : oldCitations) {
				deleteFedoraResourceService.deleteObjectCitation(uri, citation.getCitation_id());
			}		
		}
		
		return getKnowledgeObject(uri).getMetadata() ;
	}
	
	public FedoraObject createKnowledgeObject(FedoraObject fedoraObject, User loggedInUser) throws ObjectTellerException {
		return createFedoraObjectService.createObject(fedoraObject, loggedInUser);
	}

	public String getInputMessageContent(String objectURI) throws ObjectTellerException{
		return getFedoraObjectService.getObjectContent(objectURI, ChildType.INPUT.getChildType());
	}
	
	public String getOutputMessageContent(String objectURI) throws ObjectTellerException{
		return getFedoraObjectService.getObjectContent(objectURI, ChildType.OUTPUT.getChildType());
	}
	
	public String getPayloadContent(String objectURI) throws ObjectTellerException{
		return getFedoraObjectService.getObjectContent(objectURI, ChildType.PAYLOAD.getChildType());
	}
	
	public String getProvData(String objectURI) throws ObjectTellerException{
		String provDataPart1 = fusekiService.getObjectProvProperties(objectURI);

		String provDataPart2 = fusekiService.getObjectProvProperties(objectURI+"/"+ChildType.LOG.getChildType()+"/"+ChildType.CREATEACTIVITY.getChildType());

		return provDataPart1 + provDataPart2 ; 
	}
	
	public void editInputMessageContent(String objectURI,String inputMessage) throws ObjectTellerException{
		editFedoraObjectService.putBinary(inputMessage, objectURI, ChildType.INPUT.getChildType(), null);
	}
	
	public void editOutputMessageContent(String objectURI,String outputMessage) throws ObjectTellerException{
		editFedoraObjectService.putBinary(outputMessage, objectURI, ChildType.OUTPUT.getChildType(), null);
	}
	
	public Payload getPayload(String objectURI) throws ObjectTellerException {
		Payload payload = fusekiService.getPayloadProperties(objectURI);
		payload.setContent(getPayloadContent(objectURI));
		return payload ;
	}
	
	public void editPayload(String objectURI,Payload payload) throws ObjectTellerException {
		editFedoraObjectService.putBinary( payload.getContent(), objectURI, ChildType.PAYLOAD.getChildType(),null);
		editFedoraObjectService.editPayloadMetadata(payload,objectURI);
	}
}
