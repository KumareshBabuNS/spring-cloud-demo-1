$(document).ready(function() {
	$("#logVisitButton").on("click",function(){
		var name = $("#visitName").val();
		window.location = "/logVisit?name="+name;
	});
});