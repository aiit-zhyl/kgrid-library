package org.uofm.ot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uofm.ot.exception.ObjectTellerException;
import org.uofm.ot.fedoraAccessLayer.*;
import org.uofm.ot.fusekiAccessLayer.FusekiService;
import org.uofm.ot.knowledgeObject.*;
import org.uofm.ot.model.User;

import java.util.ArrayList;
import java.util.List;


@Service
public class KnowledgeObjectService {

	@Autowired
	private IdService idService;
	
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
	
	public FedoraObject getKnowledgeObject(ArkId arkId) throws ObjectTellerException {

		FedoraObject object = fusekiService.getKnowledgeObject(arkId);
		if(object != null) {
			List<Citation> citations = fusekiService.getObjectCitations(arkId);
			object.getMetadata().setCitations(citations);
		}
		return object;
	}
	
	public FedoraObject editObject(FedoraObject newObject, ArkId arkId) throws ObjectTellerException {

        String objectURI = arkId.getFedoraPath();
		addOrEditMetadata(arkId, newObject.getMetadata());
		editPayload(arkId, newObject.getPayload());
		editInputMessageContent(arkId, newObject.getInputMessage());
		editOutputMessageContent(arkId, newObject.getOutputMessage());
		FedoraObject updatedObject = getCompleteKnowledgeObject(arkId);
		return updatedObject ; 
	}
	
	public void deleteObject(ArkId arkId) throws ObjectTellerException {
		deleteFedoraResourceService.deleteObject(arkId.getFedoraPath());
	}
	
	public List<FedoraObject> getKnowledgeObjects(boolean published) throws ObjectTellerException {
		List<FedoraObject> fedoraObjects = null;
		fedoraObjects = fusekiService.getFedoraObjects(published);
		return fedoraObjects;
	}
	
	public Integer getNumberOfPublishedObjects() throws ObjectTellerException {
		return fusekiService.getNumberOfPublishedObjects();
	}
	
	public FedoraObject getCompleteKnowledgeObject(ArkId arkId) throws ObjectTellerException {

        String uri = arkId.getFedoraPath();
		
		FedoraObject object = getKnowledgeObject(arkId);
		
		Payload payload = fusekiService.getPayloadProperties(uri);
		
		payload.setContent(getPayloadContent(uri));
		
		object.setPayload(payload);

		object.setLogData(getProvData(arkId));

		object.setInputMessage(getInputMessageContent(arkId));

		object.setOutputMessage(getOutputMessageContent(arkId));
		
		return object;
	}
	
	public Metadata addOrEditMetadata(ArkId arkId, Metadata newMetadata) throws ObjectTellerException {

        String uri = arkId.getFedoraPath();

		editFedoraObjectService.editObjectMetadata(newMetadata,uri);
		editFedoraObjectService.toggleObject(uri, newMetadata.isPublished() ? "yes" : "no");
	


		List<Citation> oldCitations = fusekiService.getObjectCitations(arkId);

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
		
		if(!oldCitations.isEmpty()){
			for (Citation citation : oldCitations) {
				deleteFedoraResourceService.deleteObjectCitation(uri, citation.getCitation_id());
			}		
		}
		
		return getKnowledgeObject(arkId).getMetadata() ;
	}
	
	public FedoraObject createKnowledgeObject(FedoraObject fedoraObject, User loggedInUser) throws ObjectTellerException {
		return createFedoraObjectService.createObject(fedoraObject, loggedInUser, null);
	}

	public FedoraObject createFromExistingArkId(FedoraObject fedoraObject, ArkId existingArkId) throws ObjectTellerException {
		User loggedInUser = new User(null, null, 0, "MANUAL", "IMPORT", null);
		return createFedoraObjectService.createObject(fedoraObject, loggedInUser, existingArkId);

	}

	public String getInputMessageContent(ArkId arkId) throws ObjectTellerException{
		return getFedoraObjectService.getObjectContent(arkId.getFedoraPath(), ChildType.INPUT.getChildType());
	}
	
	public String getOutputMessageContent(ArkId arkId) throws ObjectTellerException{
		return getFedoraObjectService.getObjectContent(arkId.getFedoraPath(), ChildType.OUTPUT.getChildType());
	}
	
	public String getPayloadContent(String objectURI) throws ObjectTellerException{
		return getFedoraObjectService.getObjectContent(objectURI, ChildType.PAYLOAD.getChildType());
	}
	
	public String getProvData(ArkId arkId) throws ObjectTellerException{
		String provDataPart1 = fusekiService.getObjectProvProperties(arkId.getFedoraPath());

		String provDataPart2 = fusekiService.getObjectProvProperties(arkId.getFedoraPath()+"/"+ChildType.LOG.getChildType()+"/"+ChildType.CREATEACTIVITY.getChildType());

		return provDataPart1 + provDataPart2 ; 
	}
	
	public void editInputMessageContent(ArkId arkId,String inputMessage) throws ObjectTellerException{
		editFedoraObjectService.putBinary(inputMessage, arkId.getFedoraPath(), ChildType.INPUT.getChildType(), null);
	}
	
	public void editOutputMessageContent(ArkId arkId,String outputMessage) throws ObjectTellerException{
		editFedoraObjectService.putBinary(outputMessage, arkId.getFedoraPath(), ChildType.OUTPUT.getChildType(), null);
	}
	
	public Payload getPayload(ArkId arkId) throws ObjectTellerException {
		Payload payload = fusekiService.getPayloadProperties(arkId.getFedoraPath());
		payload.setContent(getPayloadContent(arkId.getFedoraPath()));
		return payload ;
	}
	
	public void editPayload(ArkId arkId,Payload payload) throws ObjectTellerException {
		editFedoraObjectService.putBinary( payload.getContent(), arkId.getFedoraPath(), ChildType.PAYLOAD.getChildType(),null);
		editFedoraObjectService.editPayloadMetadata(payload,arkId.getFedoraPath());
	}
	
	public void patchKnowledgeObject(FedoraObject fedoraObject,ArkId arkId) throws ObjectTellerException {
		if(fedoraObject != null){
			if(fedoraObject.getMetadata() != null ) {
				String param = fedoraObject.getMetadata().isPublished() ? "yes":"no";
				editFedoraObjectService.toggleObject(arkId.getFedoraPath(), param);
			}
		}
	}

	public boolean exists(ArkId arkId) throws ObjectTellerException {
		return getFedoraObjectService.checkIfObjectExists(arkId.getFedoraPath());
	}

	public void publishKnowledgeObject(ArkId arkId, boolean isToBePublished) throws ObjectTellerException {

		FedoraObject ko = getKnowledgeObject(arkId);

		if ( ko == null ) {
			throw new ObjectTellerException("Unable to retrieve knowledge object: " + arkId.getArkId());
		}

		editFedoraObjectService.toggleObject(arkId.getFedoraPath(), isToBePublished? "yes" : "no" );

		if(isToBePublished) {
			idService.publish(arkId);
		} else {
			idService.retract(arkId);
		}

	}
}
