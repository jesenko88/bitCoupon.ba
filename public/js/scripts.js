$(document).ready(function() {
	$('#category').change(function(e) {
		if ($('#category option:selected').text() === 'New Category')
			$('#newCategory').show();
		else
			$('#newCategory').hide();
	})
});