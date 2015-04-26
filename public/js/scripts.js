$(document).ready(function() {
	$('#category').change(function(e) {
		if ($('#category option:selected').text() === 'New Category')
			$('#newCategory').show();
		else
			$('#newCategory').hide();
	})
	
    

	  document.addEventListener("DOMContentLoaded", function() {

	    // JavaScript form validation

	    var checkPassword = function(str)
	    {
	      var re = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{6,}$/;
	      return re.test(str);
	    };

	    var checkForm = function(e)
	    {
	      
	      if(this.password.value != "" && this.password.value == this.confirmPassword.value) {
	        if(!checkPassword(this.password.value)) {
	          alert("The password you have entered is not valid!");
	          this.password.focus();
	          e.preventDefault();
	          return;
	        }
	      } else {
	        alert("Error: Please check that you've entered and confirmed your password!");
	        this.password.focus();
	        e.preventDefault();
	        return;
	      }
	      alert("Both username and password are VALID!");
	    };

	    var myForm = document.getElementById("myForm");
	    myForm.addEventListener("submit", checkForm, true);

	    // HTML5 form validation

	    var supports_input_validity = function()
	    {
	      var i = document.createElement("input");
	      return "setCustomValidity" in i;
	    }

	    if(supports_input_validity()) {
	     

	      var passwordInput = document.getElementById("field_password");
	      passwordInput.setCustomValidity(passwordInput.title);

	      var confirmPasswordInput = document.getElementById("field_confirmPassword");

	      // input key handlers

	     
	      passwordInput.addEventListener("keyup", function() {
	        this.setCustomValidity(this.validity.patternMismatch ? passwordInput.title : "");
	        if(this.checkValidity()) {
	          confirmPasswordInput.pattern = this.value;
	          confirmPasswordInput.setCustomValidity(confirmPasswordInput.title);
	        } else {
	          confirmPasswordInput.pattern = this.pattern;
	          confirmPasswordInput.setCustomValidity("");
	        }
	      }, false);

	      confirmPasswordInput.addEventListener("keyup", function() {
	        this.setCustomValidity(this.validity.patternMismatch ? confirmPasswordInput.title : "");
	      }, false);

	    }

	  }, false);
});

$(document).ready(function() {
	$('#showCoupon1').click(function(e) {
			$('#viewCoupon1').show();
			$('#showCoupon1').hide();
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

$(document).ready(function() {
	$('#forgotPass').click(function(e) {
			$('#inputEmailForm').show();
	})
});
