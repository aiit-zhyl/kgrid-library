<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page import="javax.servlet.http.HttpSession"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="<c:url value="/resources/css/style.css"/>"
	type="text/css" />
<link rel="stylesheet"
	href="<c:url value="/resources/css/bannercontent.css"/>"
	type="text/css" />
<link rel="stylesheet"
	href="<c:url value="/resources/css/datagrid.css"/>" type="text/css" />
<link rel="stylesheet" href="<c:url value="/resources/css/button.css"/>"
	type="text/css" />
<link rel="stylesheet"
	href="<c:url value="/resources/css/navigation.css"/>" type="text/css" />
<link rel="stylesheet"
	href="<c:url value="/resources/css/l_overlay.css"/>" type="text/css" />
<link rel="stylesheet" href="<c:url value="/resources/css/tab.css"/>"
	type="text/css" />
<link rel="stylesheet"
	href="<c:url value="/resources/css/formstyle.css" />" type="text/css" />
<link rel="shortcut icon"
	href="<c:url value="/resources/images/MiniIconObjectTeller.ico" /> " />
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.2/jquery.min.js"></script>
<script src="<c:url value="/resources/js/iconbutton.js"/>"></script>
<script src="<c:url value="/resources/js/scroll.js"/>"></script>
<script src="<c:url value="/resources/js/jquery.validate.js"/>"></script>
<script>

	$(document).ready(	
			function() {
				var userObj ="<%=session.getAttribute("DBUser")%>";
				var prefixhash = "#";
				var activeTab = prefixhash.concat('${ActiveTab}');
				var tabNum = $(activeTab.toLowerCase()).index();
				var nthChild = 1;
				if (tabNum >= 0) {
					var nthChild = tabNum + 1;
				}
				$("ul#tabs li.active").removeClass("active");
				$("ul#tabs li:nth-child(" + nthChild + ")").addClass("active");
				$("ul#tab li:nth-child(" + nthChild + ")").addClass("active");
				var kotitle = $("#ko-title").find("small").text();
				var title_length = kotitle.length;
				console.log("Title: "+kotitle+" Title Length: "+ title_length);
				if (title_length > 94) {
					$("#ko-title").find("small").css("font-size", "24px");
				} else {
					$("#ko-title").find("small").css("font-size", "28px");

				}
				var accessLevel = 1;
				if (userObj == "null") {
					$(".accessLevelOne").css({
						"visibility" : "hidden"
					});
					$("#session").show();
					$("#logoutsession").hide();
				} else {
					$("#session").hide();
					$("#logoutsession").show();
					switch (accessLevel) {
					default:
						$(".accessLevelOne").css({
							"visibility" : "hidden"
						});
						break;
					case 1:
						$(".accessLevelOne").css({
							"visibility" : "visible"
						});
						break;
					}
				}
				
				$("#metadataeditBtn").click(function() {
					
					//console.log($(this).attr("id"));
					var uri = $("#fObj").val();
					console.log("Current URI:"+uri);
					setURI(uri);
					setActiveTab("metadata");
					overlaySlide('addObject',true);

				});
				
				$("[id$='EditBtn']").click(function() {
					var btn_id = this.id;
					switch (btn_id){
					case "payloadEditBtn":
						var section = btn_id.replace("EditBtn", "");
						break;
					default :
						var section = btn_id.replace("EditBtn", "Message");
						break;
					}
					var uri = $("#fObj").val();
					console.log("Current URI:"+uri+ " Section:"+section);
					setURI(uri);
					//setActiveTab(section);
					overlaySlide('addObject',true);
					
				});

				$('[id^="file_"]').on('change', readMultipleFiles);
				
			});
	
	function cancelEdit() {
		$("#metadata_view").show();
		$("#metadata_edit").hide();
		$("#metadataeditBtn").text("EDIT");
		$("#deleteButton").show();
		$("#metadataeditBtn").css("left", "0%");
		$("#title_data").text(function(i, origText) {
			return origText;
		});
		$("#description_data").text(function(i, origText) {
			return origText;
		});
		$("#keyword_data").text(function(i, origText) {
			return origText;
		});
		$("#owner_data").text(function(i, origText) {
			return origText;
		});
		$("#contributor_data").text(function(i, origText) {
			return origText;
		});
	}
</script>
<script type="text/javascript">
	function toggleObject(uri, param) {
		$(this).find("span").addClass("middleout");
		$(".pri-pub .current-tab").find("span").removeClass("middleout");
		$.ajax({
			type : 'GET',
			url : "publishObject." + uri + "/" + param,
			success : function(response) {
				location.reload();
			}
		});
	}


	function deleteObject(uri) {
		var txt;
		var r = confirm("Do you really want to delete the object ? ");
		if (r == true) {
			$.ajax({
				type : 'DELETE',
				url : "deleteObject." + uri,
				success : function(response) {
					window.location.href = "home";
				}
			});
		}
	}
	
	function getSection(uri, section) {
		$.ajax({
			type : 'GET',
			url : "knowledgeObject/" + uri +"/"+section,
			success : function(response) {
				console.log("GET response:\n"+response);
				var test = JSON.stringify(response);
				//alert(test);
			},
			failure : function(response){
				console.log("GET response:\n"+response);
				var test = JSON.stringify(response);
				//alert(test);
			}
		});
	
}
	
	function displayMetadata(uri) {
			$.ajax({
				type : 'GET',
				url : "knowledgeObject/" + uri +"/metadata",
				success : function(response) {
					var test = JSON.stringify(response);
					alert(test);
				},
				failure : function(response){
					var test = JSON.stringify(response);
					alert(test);
				}
			});
		
	}
	
	
	function saveMetadata(uri) {
		var metadata = new Object();
		metadata.title = document.getElementById("title_data").value;
		metadata.contributors = document.getElementById("contributor_data").value;
		metadata.keywords = document.getElementById("keyword_data").value;
		metadata.owner = document.getElementById("owner_data").value;
		metadata.description = document.getElementById("description_data").value;
		
		var citations = [] ;
		var c = document.getElementById("citation_data_entry").childNodes ;
		var i ;
		for (i = 0; i < c.length; i++) {
	
	        if(c[i].nodeName == 'DIV'){
	        	var citation = new Object() ;
	        	var nodes = c[i].childNodes ;
	        	var j ;
	        	for (j = 0; j < nodes.length; j++) {
	        		var id = nodes[j].id ; 
	        		
	        		if (typeof id != 'undefined') {
	        			if(id.endsWith('_link')) {
	        				citation.citation_at = nodes[j].value ;
	        			}
	        		
	        			if(id.endsWith('_title')){
	        				citation.citation_title = nodes[j].value ;
	        			}
	        		
	        			if(id.endsWith('_id')) {
	        				citation.citation_id = nodes[j].value ;
	        			}
	        		}
	        	}
	        	citations.push(citation);
	        }
	    }
		
		metadata.citations = citations ; 
		
		var text = JSON.stringify(metadata);
		
		$.ajax({
			method : 'PUT',
			url : "knowledgeObject/" + uri +"/metadata",
			dataType: "json",
			data: text,
			contentType: "application/json" ,
			success : function(response) {
				var test = JSON.stringify(response);
				alert("Changes were successfully saved ");
				location.reload();
			},
			failure : function(response){
				var test = JSON.stringify(response);
				alert(test);
			}
		}); 
	}
</script>
<script type="text/javascript"
	src="<c:url value="/resources/js/dropdown.js"/>"></script>
<script src="<c:url value="/resources/js/scroll.js"/>"></script>
<script src="<c:url value="/resources/js/tabs.js"/>"></script>
<script src="<c:url value="/resources/js/custom-file-input.js"/>"></script>
<title><c:out value="${fedoraObject.metadata.title}" /></title>
</head>
<body>
	<button class="greenroundbutton" id="backtotop">
		<img src="<c:url value="/resources/images/Chevron_Icon.png"/>">
	</button>
	<div id="addObject" class="layered_overlay" aria-hidden="true">
		<%@ include file="../objects/createNewObject.jsp"%>
	</div>
	<div id="topfixed">
		<%@ include file="../common/banner.jsp"%>
	</div>
	<div id="top-stuff">
		<%@ include file="../common/navbar.jsp"%>
	</div>
	<div class="banner-content display-banner">
		<div id="goback">
			<div id="leadarrow"></div>

			<a href="<c:url value="/home"/>" id="backButton"> <spring:message
					code="BACK_RESULTS" />
			</a>
		</div>

		<c:choose>
			<c:when test="${fedoraObject.metadata.published}">
				<div class="pri-pub accessLevelOne">
					<div class="pri-pub1"
						onclick="toggleObject('${fedoraObject.URI}','no')">
						<span><spring:message code="PRIVATE" /></span>
					</div>
					<div class="pri-pub2 current-tab">
						<div class="minitype-status"></div>
						<div>
							<span class="middleout"><spring:message code="PUBLIC" /></span>
						</div>
					</div>
				</div>
			</c:when>
			<c:otherwise>
				<div class="pri-pub accessLevelOne">
					<div class="pri-pub1 current-tab">
						<span class="middleout"><spring:message code="PRIVATE" /></span>
					</div>
					<div class="pri-pub2 "
						onclick="toggleObject('${fedoraObject.URI}','yes')">
						<div class="minitype-status"></div>
						<div>
							<span><spring:message code="PUBLIC" /></span>
						</div>
					</div>
				</div>
			</c:otherwise>
		</c:choose>
		<div id="ko-title">
			<div style="width: 10px; hieght: 60px; display: inline-block;">
				<c:if test="${fedoraObject.metadata.published}">
					<div class="type-status"></div>
				</c:if>
			</div>
			<h1>
				<small><c:out value="${fedoraObject.metadata.title}"></c:out></small>
			</h1>
		</div>

		<div class="date">
			<div class="date1">
				<p class="date-title">
					<spring:message code="DATE_CREATED" />
				</p>
				<p class="date-data">
					<fmt:formatDate pattern="MMM, dd, yyyy"
						value="${fedoraObject.metadata.createdOn}" />
				</p>
			</div>
			<div class="date2">
				<p class="date-title">
					<spring:message code="UPDATE_DATE" />
					:
				</p>
				<p class="date-data">
					<fmt:formatDate pattern="MMM, dd, yyyy"
						value="${fedoraObject.metadata.lastModified}" />
				</p>
			</div>

		</div>

	</div>
	<div class="header">
		<div class="objectcontainer">
			<div class="headercol">
				<ul id="tabs">
					<li class="labels"><spring:message code="METADATA_TAB" /></li>
					<li class="labels"><spring:message code="PAYLOAD_TAB" /></li>
					<li class="labels"><spring:message code="INPUT_TAB" /></li>
					<li class="labels"><spring:message code="OUTPUT_TAB" /></li>
					<li class="labels accessLevelOne"><spring:message
							code="LOG_DATA_TAB" /></li>


				</ul>
				<div id="ellipsis" class="labels accessLevelOne">
					<img src="<c:url value="/resources/images/more.png"/> " />
				</div>
			</div>
		</div>
	</div>
	<div class="maincontentwrapper">
		<div class="main-content">
			<div class="datagrid">
				<ul id="tab">
					<li id="metadata">
						<div id="tab-content1" class="tab-content view-obj">
							<h3 class="fieldName inline">
								<spring:message code="METADATA_TAB" />
							</h3>
							<%-- <sf:form class="display-content" id="metadata_view" 
								modelAttribute="fedoraObject" action="deleteObject" method="DELETE"> --%>
							<div class="inline editwrapper accessLevelOne">
								<button class="inline edit" id="metadataeditBtn"
									style="position: relative; left: 0%"><spring:message code="EDIT_BTN" /></button>
								<%-- <button type="button" class="inline edit" id="metadataeditDisplay"
									style="position: relative; left: 0%" onclick="getSection('${fedoraObject.URI}','metadata')">Display Metadata
								</button> --%>
								<button class="inline edit" id="deleteButton"
									style="position: relative; left: 84%; bottom:-30px"
									onclick="deleteObject('${fedoraObject.URI}')">
									<spring:message code="DELETE_OBJ_BUTTON" />
								</button>
							</div>
							<form class="display-content" id="metadata_view">
								<div class="addtext">
									<h4>
										<spring:message code="OBJECT_TITLE" />
									</h4>
									<input type="text" class="metaEdit" id="title_data_v" disabled
										value="${fedoraObject.metadata.title}" />
								</div>
								<div class="addtext">
									<h4>
										<spring:message code="OBJECT_DESCRIPTION" />
									</h4>
									<textarea class="metaEdit" id="description_data_v" disabled>${fedoraObject.metadata.description}</textarea>
								</div>
								<div class="addtext">
									<h4>
										<spring:message code="OBJECT_KEYWORD" />
									</h4>
									<input type="text" class="metaEdit" id="keyword_data_v"
										disabled value="${fedoraObject.metadata.keywords}" />
								</div>
								<div class="addtext">
									<h4>
										<spring:message code="OBJECT_OWNERS" />
									</h4>
									<input type="text" class="metaEdit" id="owner_data_v" disabled
										value="${fedoraObject.metadata.owner}" />
								</div>
								<div class="addtext">
									<h4>
										<spring:message code="OBJECT_CONTRIBUTORS" />
									</h4>
									<input type="text" class="metaEdit" id="contributor_data_v"
										disabled value="${fedoraObject.metadata.contributors}" />
								</div>
								<div class="addtext">
									<h4>
										<spring:message code="OBJECT_CITATIONS" />
									</h4>

									<c:forEach var="citationEntry"
										items="${fedoraObject.metadata.citations}"
										varStatus="loopStatus">
										<div>
											<input type="text" class="metaEdit" disabled
												value="${citationEntry.citation_title}"><input
												type="hidden" class="metaEdit"
												value="${citationEntry.citation_at}">
										</div>
									</c:forEach>
								</div>
	
								<input type="hidden" id="fObj" path="URI" value="${fedoraObject.URI}" />
							</form>
						</div>
					</li>
					<li id="payload">
						<div id="tab-content2" class="tab-content view-obj">
							<h3 class="fieldName inline">
								<spring:message code="PAYLOAD_TAB" />
							</h3>
							<div class="inline editwrapper accessLevelOne">
								<button class="inline edit" id="payloadEditBtn"
									style="position: relative; left: 0%"><spring:message code="EDIT_BTN" /></button>
							</div>

							<form class="display-content" id="displaypayload">
								<div>
									<h4>
										<spring:message code="PAYLOAD_FUNCTION" />
										<spring:message code="REQUIRED_FIELD" />
									</h4>
									<input type="text" class="metaEdit" id="functionname_data"
										disabled
										value="${fedoraObject.payload.functionName}">
								</div>
								<div>
									<h4>
										<spring:message code="PAYLOAD_TYPE" />
									</h4>
									<input type="text" class="metaEdit" id="enginetype_data"
										disabled value="${fedoraObject.payload.engineType}">
								</div>
								<div class="display-payload">
									<textarea class="autosize" id="payloadTextArea-v">${fedoraObject.payload.content}</textarea>
								</div>
							</form>
						</div>
					</li>
					<li id="input">
						<div id="tab-content3" class="tab-content view-obj">
							<h3 class="fieldName inline">
								<spring:message code="INPUT_MESSAGE" />
							</h3>
							<div class="inline editwrapper accessLevelOne">
								<button class="inline edit" id="inputEditBtn"
									style="position: relative;"><spring:message code="EDIT_BTN" /></button>
								
							</div>
							<form class="display-content" id="displayinput">
								<div class="display-payload">
									<textarea class="autosize" id="inputTextArea-v">${fedoraObject.inputMessage}</textarea>
								</div>
							</form>

						</div>
					</li>
					<li id="output">
						<div id="tab-content4" class="tab-content view-obj">
							<h3 class="fieldName inline">
								<spring:message code="OUTPUT_MESSAGE" />
							</h3>
							<div class="inline editwrapper accessLevelOne">
								<button class="inline edit" id="outputEditBtn"
									style="position: relative; left: 0%"><spring:message code="EDIT_BTN" /></button>
									<%-- <button type="button" class="inline edit" id="metadataeditDisplay"
									style="position: relative; left: 0%" onclick="getSection('${fedoraObject.URI}','outputMessage')">Display Output
								</button>  --%>
							</div>
							<form class="display-content" id="displayoutput">
								<div class="display-payload">
									<textarea class="autosize" id="outputTextArea-v">${fedoraObject.outputMessage}</textarea>
								</div>
							</form>
							<sf:form class="display-content" id="outputEdit" method="POST"
								style="display:none;position: relative;"
								action="editOutputMessage" modelAttribute="fedoraObject">
								<button type="submit" class="done" id="saveOutputButton">
									<spring:message code="SAVE_CHANGES_BTN" />
								</button>
								<div id="outputDropFile" class="dropfile" style="display: none;">
									<div class="upload-direction">
										<input type="file" name="file_output" id="file_output"
											class="inputfile"
											data-multiple-caption="{count} files selected" multiple
											style="display: none;" /> <label class="upload_label"
											for="file_output"> <img
											src="<c:url value="/resources/images/Upload_Icon.png"/> " />
											<br>
											<p class="green">
												<label for="file_payload"><spring:message
														code="CHOOSE_FILE" /></label>
											</p> <br>
											<p>
												<spring:message code="FILE_TYPE" />
											</p>
										</label>
									</div>
									<p class="instruction">
										<spring:message code="CLICK" />
										<a href=#><spring:message code="HERE" /></a>
										<spring:message code="OUTPUT_MESSAGE_DOWNLOAD" />
									</p>
								</div>
								<div class="display-output" id="outputTextAreaDisplay">
									<button id="outputClearBtn">
										<spring:message code="REMOVE_BTN" />
									</button>
									<sf:textarea class="autosize" id="outputTextArea"
										path="outputMessage"></sf:textarea>
									<sf:input type="hidden" path="URI" value="${fedoraObject.URI}" />
								</div>
							</sf:form>
						</div>
					</li>
					<li id="logdata">
						<div id="tab-content5" class="tab-content view-obj">
							<h3 class="fieldName inline ">
								<spring:message code="LOG_DATA_TAB" />
							</h3>
							<section class="display-content">
							<div class="display-payload">
								<p>${processedLogData}</p>
							</div>
							</section>
						</div>
					</li>

				</ul>
			</div>
		</div>
	</div>
</body>
</html>