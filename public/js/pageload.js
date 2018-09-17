$(window).load(function() {
	// Animate loader off screen
	$(".se-pre-con").fadeOut("fast");
});

$(window).bind('beforeunload', function() {
	// Bring back before page leave
	$(".se-pre-con").fadeIn("slow");
});

$(document).on('pagehide', function() {
	// Bring back before page leave
	$(".se-pre-con").fadeIn("slow");
}); 