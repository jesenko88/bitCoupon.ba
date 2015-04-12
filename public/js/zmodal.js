var loginLink = document.getElementById('modalInfo');

loginLink.onclick = function(e) {
	e.preventDefault();

	var loginForm = document.getElementById('subscribeModal');
	loginForm.style.display = 'block';

	var overlay = document.createElement('div');
	overlay.className = 'overlay';

	var body = document.querySelector('body');
	body.appendChild(overlay);
};

var closeModalButtons = document.querySelectorAll('.close-modal');
var closeModal = function() {
		var modal = this.parentElement;
		modal.style.display = 'none';

		var overlay = document.querySelector('.overlay');
		overlay.parentElement.removeChild(overlay);
	};

for (var i = 0; i < closeModalButtons.length; i++) {
	closeModalButtons[i].onclick = closeModal;
}