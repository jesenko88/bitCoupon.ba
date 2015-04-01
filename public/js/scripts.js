$(document).ready(function() {
	$('#category').change(function(e) {
		if ($('#category option:selected').text() === 'New Category')
			$('#newCategory').show();
		else
			$('#newCategory').hide();
	})
});

$(document).ready(function() {
	$('#companySignup').click(function(e) {
			$('#companyRegistration').show();

			$('#someId').hide();
	})
});

$(document).ready(function() {
	$('#signup').click(function(e) {
			$('#companyRegistration').hide();

			$('#someId').show();
	})
});