// `status` tooltip
$('.status').powerTip({
	placement: 'nw-alt',
	closeDelay: 0
});
$('.status').data('powertip', 'All our systems are <span style="color:mediumaquamarine;font-size:1.1em"><i>100%</i></span> :-)');

if ($(window).width() > 800) {
	// `terms` tooltip
	$('.terms').powerTip({
		placement: 'nw-alt',
		closeDelay: 0
	});
	$('.terms').data('powertip', "Find out what you're agreeing to when you use our service");

	// `help` tooltip
	$('.help-f').powerTip({
		placement: 'nw-alt',
		closeDelay: 0
	});
	$('.help-f').data('powertip', 'Forgot your password?');

	// `brand` tooltip
	$('.brand-f').powerTip({
		placement: 'ne-alt',
		closeDelay: 0
	});
	$('.brand-f').data('powertip', 'Our design process and theme foundations');

	// `contact` tooltip
	$('.contact').powerTip({
		placement: 'ne-alt',
		closeDelay: 0
	});
	$('.contact').data('powertip', 'Say hi and send inquiries');

	// `about` tooltip
	$('.about').powerTip({
		placement: 'ne-alt',
		closeDelay: 0
	});
	$('.about').data('powertip', "Learn who we are and what we're about");
}