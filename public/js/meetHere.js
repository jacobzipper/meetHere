// Modal
$(".modal").each( function(){
	$(this).wrap('<div class="overlay"></div>')
});

$('#open-modal, #open-modal-1').on('touchend || click', function(e){
	e.preventDefault();
	e.stopImmediatePropagation();

	var $this = $(this),
			modal = $($this).data('modal');

	$(modal).parents('.overlay').addClass('open');
	setTimeout( function(){
		$(modal).addClass('open');
	}, 350);

	$(document).on('touchend || click', function(e){
		var target = $(e.target);

		if ($(target).hasClass('overlay')){
			$(target).find('.modal').each( function(){
				$(this).removeClass('open');
			});
			setTimeout( function(){
				$(target).removeClass('open');
			}, 350);
		}

	});


	if ($('#login-username').val() != "" && $('#login-password').val() != "")
		$('#login-note-button').removeClass('disabled').css('cursor', 'pointer');

});

var loginStatus;

$('.close-modal').on('touchend || click', function(e){
	e.preventDefault();
	e.stopImmediatePropagation();

	var $this = $(this),
			modal = $($this).data('modal');

	/* XHR
	var data = "username=hafiz&password=runaway";

	var xhr = new XMLHttpRequest();
	xhr.withCredentials = true;

	xhr.addEventListener("readystatechange", function () {
		if (this.readyState === 4) {
			console.log(this.responseText);
			var jn = JSON.parse(this.responseText);
			loginStatus = jn.error;

			if (loginStatus == 0) {
				$(modal).removeClass('open');
				setTimeout( function(){
					$(modal).parents('.overlay').removeClass('open');
				}, 350);
				var prefs = {
					"async": true,
					"crossDomain": true,
					"url": "https://meethere.herokuapp.com/getfriends",
					"method": "POST",
					"xhrFields": {
						"withCredentials": true
					},
					"data": {
					}
				}

				$.ajax(prefs).done(function (response) {
				  console.log(response);
				});
				window.location.href = './map';
			} else {
				if (loginStatus == 1) {
					$('#login-status').text('Wrong password').addClass('fade-out-opacity');
					setTimeout(function() {
						$('#login-status').removeClass('fade-out-opacity');
					}, 2000);
				} else if (loginStatus == 2) {
					$('#login-status').text('User not found').addClass('fade-out-opacity');
					setTimeout(function() {
						$('#login-status').removeClass('fade-out-opacity');
					}, 2000);
				} else {
					$('#login-status').text('Incorrect Parameters').addClass('fade-out-opacity');
					setTimeout(function() {
						$('#login-status').removeClass('fade-out-opacity');
					}, 2000);
				}
				$('#login-username').val('').val('');
				$('#login-password').val('').val('');
				$('#login-note-button').addClass('disabled').css('cursor', 'default');
			}
		}
	});

	xhr.open("POST", "https://meethere.herokuapp.com/signin");
	xhr.send(data);
	*/
	var settings = {
		"async": true,
		"crossDomain": true,
		"url": "https://meethere.herokuapp.com/signin",
		"method": "POST",
		"xhrFields": {
			"withCredentials": true
		},
		"data": {
			"username": $('#login-username').val(),
			"password": $('#login-password').val()
		}
	}

	$.ajax(settings).done(function(data) {
		loginStatus = data.error;
		if (loginStatus == 0) {
			$(modal).removeClass('open');
			setTimeout( function(){
				$(modal).parents('.overlay').removeClass('open');
			}, 350);
			var prefs = {
				"async": true,
				"crossDomain": true,
				"url": "https://meethere.herokuapp.com/getfriends",
				"method": "POST",
				"xhrFields": {
					"withCredentials": true
				},
				"data": {
				}
			}

			$.ajax(prefs).done(function (response) {
			  console.log(response);
			});
			window.location.href = './map';
		} else {
			if (loginStatus == 1) {
				$('#login-status').text('Wrong password').addClass('fade-out-opacity');
				setTimeout(function() {
					$('#login-status').removeClass('fade-out-opacity');
				}, 2000);
			} else if (loginStatus == 2) {
				$('#login-status').text('User not found').addClass('fade-out-opacity');
				setTimeout(function() {
					$('#login-status').removeClass('fade-out-opacity');
				}, 2000);
			} else {
				$('#login-status').text('Incorrect Parameters').addClass('fade-out-opacity');
				setTimeout(function() {
					$('#login-status').removeClass('fade-out-opacity');
				}, 2000);
			}
			$('#login-username').val('').val('');
			$('#login-password').val('').val('');
			$('#login-note-button').addClass('disabled').css('cursor', 'default');
		}
	});

	// login__POST
	/*
	$.post('https://meethere.herokuapp.com/signin', {
		username: $('#login-username').val(),
		password: $('#login-password').val()
	}, function(data) {
		loginStatus = data.error;
		if (loginStatus == 0) {
			$(modal).removeClass('open');
			setTimeout( function(){
				$(modal).parents('.overlay').removeClass('open');
			}, 350);
			$.post('https://meethere.herokuapp.com/getfriends', {
					username: 'test',
					password: 'test',
					email: 'test@as.com'
				}, function(data) {
					console.log(data)
				}, "json"
			);
		} else {
			if (loginStatus == 1) {
				$('#login-status').text('Wrong password').addClass('fade-out-opacity');
				setTimeout(function() {
					$('#login-status').removeClass('fade-out-opacity');
				}, 2000);
			} else if (loginStatus == 2) {
				$('#login-status').text('User not found').addClass('fade-out-opacity');
				setTimeout(function() {
					$('#login-status').removeClass('fade-out-opacity');
				}, 2000);
			} else {
				$('#login-status').text('Incorrect Parameters').addClass('fade-out-opacity');
				setTimeout(function() {
					$('#login-status').removeClass('fade-out-opacity');
				}, 2000);
			}
			$('#login-username').val('').val('');
			$('#login-password').val('').val('');
			$('#login-note-button').addClass('disabled').css('cursor', 'default');
		}
	}, "json"
	);
	*/
});

$(document).keyup(function(e) {
	if (e.which == 13 && $('#modal1').hasClass('open'))
		$('.close-modal').click();
	if ($('#login-username').val() != "" && $('#login-password').val() != "")
		$('#login-note-button').removeClass('disabled').css('cursor', 'pointer');
	else
		$('#login-note-button').addClass('disabled').css('cursor', 'default');
});

// Validation
var validateEmail = function(email) {
	return /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/.test(email);
}

var validateFullName = function(name) {
	return /^[A-Za-z\s]+$/.test(name)
}

var validateName = function(name) {
	return /^[a-zA-Z]+$/.test(name);
}

var validateUserName = function(name) {
	return /^[a-z0-9.-]+$/.test(name);
}

var validatePhone = function(phone) {
	return /^[(]{0,1}[0-9]{3}[)]{0,1}[-\s\.]{0,1}[0-9]{3}[-\s\.]{0,1}[0-9]{4}$/.test(phone);
}

function isLowerCase(char)
{
    return char == char.toLowerCase() && char != char.toUpperCase();
}