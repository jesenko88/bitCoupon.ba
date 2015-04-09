$(document).ready(function() {
	
	$('#category').change(function(e) {
		if ($('#category option:selected').text() === 'New Category')
			$('#newCategory').show();
		else
			$('#newCategory').hide();
	})
	
	$('#signup').click(function(e) {
			$('#companyRegistration').hide();

			$('#someId').show();
	})
	
	$('#companySignup').click(function(e) {
			$('#companyRegistration').show();

			$('#someId').hide();
	})
	
	$('#forgotPass').click(function(e) {
			$('#inputEmailForm').show();
	})
	
	$('#dob').change(function(e){
		if(!calculateDate(new Date(this.value))){
			$('#error').show();
		} else {
			$('#error').hide();
		}
	})
	
	$('#maxOrder').change(function(e){
		var minOrder = document.getElementById("minOrder").value;
		var maxOrder = document.getElementById("maxOrder").value;
		if(maxOrder < minOrder){
			$('#error').show();
		} else {
			$('#error').hide();
		}
	});
	
});

function calculateDate(birth){
	var today = new Date();

	var age = today.getFullYear() - birth.getFullYear();
	var age_month = today.getMonth() - birth.getMonth();
	var age_day = today.getDate() - birth.getDate();

	if (age_month < 0 || (age_month == 0 && age_day < 0)) {
	    age = parseInt(age) - 1;
	}
	return age >= 18;
}
