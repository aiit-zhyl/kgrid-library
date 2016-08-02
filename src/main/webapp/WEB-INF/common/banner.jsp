<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page import="javax.servlet.http.HttpSession" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript">

$(document).ready(function(){
	
/* 		alert("inside banner test"); */
		var userObj = "<%=session.getAttribute("DBUser")%>";
		if ('null' != userObj) {
			$("#session").hide();
			$("#logoutsession").show();
			$("#user-link").val("Hello, "+userObj.first_name);
		} else {
			$("#session").show();
			$("#logoutsession").hide();
		}

		$("#login").click( function login() {
	
		var user = new Object();
		user.username = document.getElementById("username").value;
		user.passwd = document.getElementById("passwd").value;

		var text = JSON.stringify(user);
 		$.ajax({
					beforeSend : function(xhrObj) {
						xhrObj.setRequestHeader("Content-Type",
								"application/json");
						xhrObj.setRequestHeader("Accept", "application/json");
					},
					type : 'POST',
					url : "login",
					data : text,
					dataType : "json",

					success : function(response) {
					 if(response!='empty') {
							  var test = JSON.stringify(response);
						      var obj = JSON.parse(test);
						      location.reload();
					    }
					} ,
					
					error : function(response) {
						// TODO: Handle Error Message
						alert(response.responseText);
					}
				});
		}); 
		
		
		$("#logout").click(function userLogout(){
			$.ajax({
				type : 'POST',
				url : "logout" ,

				success : function(response) {
					location.reload();
				}
			});
		});

	});
</script>
</head>
<body>
	<div id="logo"><a href="home">
		<img src="<c:url value="/resources/images/logo.png"/>" width="200px"
			height="auto"></a>
	</div>
			<div class="active-links">
				
					<div id="session" class="login-link">
						<a id="signin-link" href="#"> <strong><spring:message
									code="LOG_IN_BUTTON" /></strong>
						</a>
					</div>
					<div id="signin-dropdown" class="dropdown">

					<form  class="signin">
							<fieldset class="signin-textbox">
								<input name="username" type="text" id="username"
									autocomplete="on" placeholder="Username"></input>
								<input type="password"  placeholder="Password" id="passwd"
									name="passwd" autocomplete="off"></input>
							</fieldset>
							<fieldset class="remb">
								<label class="remember"> <input type="checkbox"
									value="1" name="remember_me" /> <span><spring:message
											code="REMEMBER_ME_BUTTON" /></span>
								</label>
								<button id="login" class="submit button" type="button">
									<spring:message code="LOG_IN_BUTTON" />
								</button>
							</fieldset>
							<p>
								<a href="retrievepw.html"> <spring:message
										code="PASSWORD_RESET" /></a>
							</p>
						</form>
					</div>
					
				<!-- 
					<div id="logout"> -->
					<div id="logoutsession" class="logout-link">
						<ul>
<li id="user"><a id="user-link" href="#"> <strong><spring:message
								code="HELLO_MESSAGE" /> ${loggedInUser.first_name} </strong>
				</a></li>
							<li id="icon" class="down"><img id="iconimg" class="down"
								src="<c:url value="/resources/images/Chevron_Icon.png"/>" width="12px" /></li>
						</ul>
					</div>
					<div id="logout-dropdown" class="dropdown">
						<form method="POST" class="signin" action="">
							<fieldset class="links">
								<button id="settings" type="button" class="open-overlay button" onclick="openNav()">
									<spring:message code="SETTINGS" />
								</button>
							</fieldset>
						</form>
						<form class="signin">
							<fieldset class="links">
								<button id="logout" class="submit button" type="button">
									<spring:message code="LOGOUT" />
								</button>
							</fieldset>
						</form>
						</div>
					<!-- </div> -->
				
			</div>
		

</body>
</html>