function remove(arr, what) {
    var found = arr.indexOf(what);

    while (found !== -1) {
        arr.splice(found, 1);
        found = arr.indexOf(what);
    }
}
function phoneNumber(inputtxt)  {  
	var phoneno = /^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$/;  

	if((inputtxt.match(phoneno))) {
		return true;
	}
	else {
		return false;
	}
}
function headers(res) {
	res.header("Access-Control-Allow-Origin", "https://meethereapp.github.io");
	res.header("Access-Control-Allow-Credentials", "true");
	res.header("Strict-Transport-Security", "max-age=31536000");
}

var express = require('express');
var router = express.Router();
var pg = require('pg');
var crypto = require('crypto');
var MersenneTwister = require('mersenne-twister');
var generator = new MersenneTwister();
var twilioClient = require('twilio')('REDACTED', 'REDACTED');

pg.defaults.ssl = true;
var url = process.env.DATABASE_URL || "REDACTED";
pg.connect(url, function(err, client) {
	if (err) throw err;
	console.log('Connected to postgres! Getting schemas...');
	signin(client);
	signup(client);
	usernameCheck(client);
	getFriends(client);
	addFriend(client);
	addFriendTemp(client);
	subFriend(client);
	fullname(client);
	getInPending(client);
	getOutPending(client);
	getLocation(client);
	updateLocation(client);
	getLocationUser(client);
	sendTexts(client);
	getNotifications(client);
	logout();
	home();
	help();
	map();
	signupview();
	brand();
	contact();
	about();
});
function signin(client) {
	router.post('/signin',function(req,res,next) {
		headers(res);
		var username = req.body.username;
		var password = req.body.password;
		if(username && password) {
			client.query({
				text: 'SELECT * FROM users WHERE username=$1;',
				values: [username]
			}, function(err, result) {
				if(result.rows.length) {
					var pass = crypto.createHash('sha256').update(password+result.rows[0].salt).digest('base64');
					if(pass==result.rows[0].password) {
						req.session.username = username;
						res.json({error:0});
					}
					else {
						res.json({error:1});
					}
				}
				else {
					res.json({error:2});
				}
			});
		}
		else {
			res.json({error:3});
		}
		res.status(200);
	});
}
function signup(client) {
	router.post('/signup',function(req,res,next) {
		headers(res);
		var username = req.body.username;
		var password = req.body.password;
		var email = req.body.email;
		if(username && password && email) {
			client.query({
					text: 'SELECT * FROM users WHERE username=$1;',
					values: [username]
			}, function(err, result) {
				if(result.rows.length) {
					res.json({error:1});
				}
				else {
					var salt = generator.random_int31()+""+generator.random_int31()+""+generator.random_int31()+""+generator.random_int31();
					var saltedpass = crypto.createHash('sha256').update(password+salt).digest('base64');
					client.query({
						text: 'INSERT INTO users (username, password, salt, email, phone, fullname,latitude,longitude) VALUES ($1,$2,$3,$4,$5,$6,$7,$8);',
						values: [username,saltedpass,salt,email,(req.body.phone || ""),(req.body.fullname || ""),(req.body.latitude || 0), (req.body.longitude || 0)]
					});
					res.json({error:0});
				}
			});
		}
		else {
			res.json({error:2});
		}
		res.status(200);
	});
}
function usernameCheck(client) {
	router.post('/usernamecheck',function(req,res,next) {
		headers(res);
		var username = req.body.username;
		if(username) {
			client.query({
					text: 'SELECT * FROM users WHERE username=$1;',
					values: [username]
			}, function(err, result) {
				if(result.rows.length) {
					res.json({error:0});
				}
				else {
					res.json({error:1});
				}
			});
		}
		else {
			res.json({error: 2});
		}
		res.status(200);
	});
}
function getFriends(client) {
	router.post('/getfriends',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		if(username) {
			client.query({
					text: 'SELECT * FROM users WHERE username=$1;',
					values: [username]
			}, function(err, result) {
				if(result.rows.length) {
					if(result.rows[0].notifications[0]=="2") {
						client.query({
							text: 'UPDATE users SET notifications=$1 WHERE username=$2;',
							values: [["0","0"], username]
						});
					}
					res.json({error:0, friends: result.rows[0].friends});
				}
				else {
					res.json({error:1});
				}
			});
		}
		else {
			res.json({error: 2});
		}
		res.status(200);
	});
}

function fullname(client) {
	router.post('/fullname',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		if(username) {
			client.query({
					text: 'SELECT fullname FROM users WHERE username=$1;',
					values: [username]
			}, function(err, result) {
				if(result.rows.length) {
					res.json({error:0, fullname: result.rows[0].fullname});
				}
				else {
					res.json({error:1});
				}
			});
		}
		else {
			res.json({error: 2});
		}
		res.status(200);
	});
}

function updateLocation(client) {
	router.post('/updatelocation',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		var lat = req.body.latitude;
		var long = req.body.longitude;
		if(username && lat!=232323223.352342534 && long!=232323223.352342534) {
			client.query({
					text: 'UPDATE users SET latitude=$1,longitude=$2 WHERE username=$3;',
					values: [lat,long,username]
			}, function(err, result) {
				res.json({error:0});
			});
		}
		else {
			res.json({error: 1});
		}
		res.status(200);
	});
}
function addFriend(client) {
	router.post('/addfriend',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		var friend = req.body.friend;
		console.log(phoneNumber('9547401737'));
		if(username && friend && friend!=username) {
			client.query({
					text: 'SELECT * FROM users WHERE username=$1 OR username=$2;',
					values: [username,friend]
			}, function(err, result) {
				if(result.rows.length==2) {
					if(result.rows[1].username == username) {
						var b = result.rows[0];
						result.rows[0] = result.rows[1];
						result.rows[1] = b;
					}
					var userFriends = result.rows[0].friends;
					var userInPending = result.rows[0].inpending;
					var userOutPending = result.rows[0].outpending;
					var friendFriends = result.rows[1].friends;
					var friendInPending = result.rows[1].inpending;
					var friendOutPending = result.rows[1].outpending;
					if(userFriends.indexOf(friend) >-1) {
						res.json({error:2});
					}
					else if(userInPending.indexOf(friend) > -1) {
						userFriends.push(friend);
						remove(userInPending,friend);
						remove(friendOutPending,username);
						friendFriends.push(username);
						client.query({
							text: 'UPDATE users SET friends=$1,inpending=$2 WHERE username=$3;',
							values: [userFriends, userInPending, username]
						});
						client.query({
							text: 'UPDATE users SET friends=$1,outpending=$2,notifications=$3 WHERE username=$4;',
							values: [friendFriends,friendOutPending,["2",username], friend]
						});
						res.json({error:0});
					}
					else if(userOutPending.indexOf(friend) > -1) {
						res.json({error: 3});
					}
					else {
						userOutPending.push(friend);
						friendInPending.push(username)
						client.query({
							text: 'UPDATE users SET outpending=$1 WHERE username=$2;',
							values: [userOutPending, username]
						});
						client.query({
							text: 'UPDATE users SET inpending=$1,notifications=$2 WHERE username=$3;',
							values: [friendInPending,["1",username], friend]
						});
						res.json({error:0});
						
					}
				}
				else {
					res.json({error:1});
				}
			});
		}
		else {
			res.json({error: 4});
		}
		res.status(200);
	});
}

function addFriendTemp(client) {
	router.post('/addfriendtemp',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		var friend = req.body.friend;
		if(username && friend && friend!=username) {
			client.query({
					text: 'SELECT * FROM users WHERE username=$1 OR username=$2;',
					values: [username,friend]
			}, function(err, result) {
				if(result.rows.length==2) {
					if(result.rows[1].username == username) {
						var b = result.rows[0];
						result.rows[0] = result.rows[1];
						result.rows[1] = b;
					}
					var userFriends = result.rows[0].friends;
					var userInPending = result.rows[0].inpending;
					var userOutPending = result.rows[0].outpending;
					var friendFriends = result.rows[1].friends;
					var friendInPending = result.rows[1].inpending;
					var friendOutPending = result.rows[1].outpending;
					if(userFriends.indexOf(friend) >-1) {
						res.json({error:2});
					}
					else if(userInPending.indexOf(friend) > -1) {
						userFriends.push(friend);
						remove(userInPending,friend);
						remove(friendOutPending,username);
						friendFriends.push(username);
						res.json({error:0});
					}
					else if(userOutPending.indexOf(friend) > -1) {
						res.json({error: 3});
					}
					else {
						userOutPending.push(friend);
						friendInPending.push(username);
						res.json({error:0});
						
					}
				}
				else {
					res.json({error:1});
				}
			});
		}
		else {
			res.json({error: 4});
		}
		res.status(200);
	});
}
function subFriend(client) {
	router.post('/subfriend',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		var friend = req.body.friend;
		if(username && friend) {
			client.query({
					text: 'SELECT * FROM users WHERE username=$1 OR username=$2;',
					values: [username,friend]
			}, function(err, result) {
				console.log(result.rows);
				console.log(username > friend);
				if(result.rows.length==2) {
					if(result.rows[1].username == username) {
						var b = result.rows[0];
						result.rows[0] = result.rows[1];
						result.rows[1] = b;
					}
					var userFriends = result.rows[0].friends;
					var userInPending = result.rows[0].inpending;
					var userOutPending = result.rows[0].outpending;
					var friendFriends = result.rows[1].friends;
					var friendInPending = result.rows[1].inpending;
					var friendOutPending = result.rows[1].outpending;
					if(userFriends.indexOf(friend) >-1) {
						remove(userFriends,friend);
						remove(friendFriends,username);
						client.query({
							text: 'UPDATE users SET friends=$1 WHERE username=$2;',
							values: [userFriends, username]
						});
						client.query({
							text: 'UPDATE users SET friends=$1 WHERE username=$2;',
							values: [friendFriends, friend]
						});
						res.json({error:0});
					}
					else if(userInPending.indexOf(friend) > -1) {
						remove(userInPending,friend);
						remove(friendOutPending,username);
						client.query({
							text: 'UPDATE users SET inpending=$1 WHERE username=$2;',
							values: [userInPending, username]
						});
						client.query({
							text: 'UPDATE users SET outpending=$1 WHERE username=$2;',
							values: [friendOutPending, friend]
						});
						res.json({error:0});
					}
					else if(userOutPending.indexOf(friend) > -1) {
						remove(userOutPending,friend);
						remove(friendInPending,username);
						client.query({
							text: 'UPDATE users SET outpending=$1 WHERE username=$2;',
							values: [userOutPending, username]
						});
						client.query({
							text: 'UPDATE users SET inpending=$1 WHERE username=$2;',
							values: [friendInPending, friend]
						});
						res.json({error:0});
					}
					else {
						res.json({error:2});
						
					}
				}
				else {
					res.json({error:1});
				}
			});
		}
		else {
			res.json({error: 3});
		}
		res.status(200);
	});
}

function getInPending(client) {
	router.post('/getinpending',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		if(username) {
			client.query({
					text: 'SELECT * FROM users WHERE username=$1;',
					values: [username]
			}, function(err, result) {
				if(result.rows.length) {
					if(result.rows[0].notifications[0]=="1") {
						client.query({
							text: 'UPDATE users SET notifications=$1 WHERE username=$2;',
							values: [["0","0"], username]
						});
					}
					res.json({error:0, friends: result.rows[0].inpending});
				}
				else {
					res.json({error:1});
				}
			});
		}
		else {
			res.json({error: 2});
		}
		res.status(200);
	});
}
function getOutPending(client) {
	router.post('/getoutpending',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		if(username) {
			client.query({
					text: 'SELECT outpending FROM users WHERE username=$1;',
					values: [username]
			}, function(err, result) {
				if(result.rows.length) {
					res.json({error:0, friends: result.rows[0].outpending});
				}
				else {
					res.json({error:1});
				}
			});
		}
		else {
			res.json({error: 2});
		}
		res.status(200);
	});
}
function getLocation(client) {
	router.post('/getlocation',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		var friend = req.body.friend;
		if(username && friend) {
			client.query({
					text: 'SELECT friends FROM users WHERE username=$1;',
					values: [username]
			}, function(err, result) {
				if(result.rows[0].friends.indexOf(friend)>-1) {
					client.query({
						text: 'SELECT * FROM users WHERE username=$1;',
						values: [friend]
					}, function(err, result) {
						if(result.rows.length) {
							var resultStuff = result.rows[0];
							res.json({error:0, latitude: resultStuff.latitude, longitude: resultStuff.longitude});
						}
						else {
							res.json({error:1});
						}
					});
				}
				else {
					res.json({error:2});
				}
			});
		}
		else {
			res.json({error: 3});
		}
		res.status(200);
	});
}

function getLocationUser(client) {
	router.post('/getlocationuser',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		if(username) {
			client.query({
					text: 'SELECT * FROM users WHERE username=$1;',
					values: [username]
			}, function(err, result) {
				if(result.rows.length) {
					res.json({error:0, latitude: result.rows[0].latitude, longitude:result.rows[0].longitude});
				}
				else {
					res.json({error:1});
				}
			});
		}
		else {
			res.json({error: 2});
		}
		res.status(200);
	});
}

function sendTexts(client) {
	router.post('/sendtexts',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		var name = req.body.name;
		var addy = req.body.location;
		var friends = JSON.parse(req.body.friends);
		if(username && name && addy && friends) {
			for(var fri in friends) {
				var friend = friends[fri];
				if(phoneNumber(friend)) {
					twilioClient.sendMessage({
						to: friend,
						from: '+15617085832 ',
						body: 'Hello from meetHere! Your friend '+username+' wants to meet up with you at '+name+'. If you don\'t know where that is, here\'s the address: '+addy
					}, function(err, responseData) {
						if (!err) {
							console.log(responseData.from);
							console.log(responseData.body);
							res.json({error:0});
						}
						else {
							res.json({error:1});
						}
					});
				}
				else {
					client.query({
						text: 'SELECT phone FROM users WHERE username=$1;',
						values: [friend]
					}, function(err, result) {
						if(result.rows.length) {
							twilioClient.sendMessage({
								to: result.rows[0].phone,
								from: '+15617085832 ',
								body: 'Hello from meetHere! Your friend '+username+' wants to meet up with you at '+name+'. If you don\'t know where that is, here\'s the address: '+addy
							}, function(err, responseData) {
								if (!err) {
									console.log(responseData.from);
									console.log(responseData.body);
									res.json({error:0});
								}
								else {
									res.json({error:1});
								}
							});
						}
					});
				}
			}
		}
		else {
			res.json({error: 2});
		}
		res.status(200);
	});
}
function getNotifications(client) {
	router.post('/getnotifications',function(req,res,next) {
		headers(res);
		var username = req.session.username;
		if(username) {
			client.query({
					text: 'SELECT notifications FROM users WHERE username=$1;',
					values: [username]
			}, function(err, result) {
				if(result.rows.length) {
					var notif = result.rows[0].notifications;
					if(notif[0]=="0") {
						res.json({error:0,notifs:0,name:""});
					}
					else if(notif[0]=="1") {
						client.query({
							text: 'UPDATE users SET notifications=$1 WHERE username=$2;',
							values: [["0","0"], username]
						});
						res.json({error:0,notifs:1,name:notif[1]});
					}
					else if(notif[0]=="2") {
						client.query({
							text: 'UPDATE users SET notifications=$1 WHERE username=$2;',
							values: [["0","0"], username]
						});
						res.json({error:0,notifs:2,name:notif[1]});
					}
				}
				else {
					res.json({error:1});
				}
			});
		}
		else {
			res.json({error: 2});
		}
		res.status(200);
	});
}
function logout() {
	router.get('/logout',function(req,res,next) {
		headers(res);
		req.session.username=null;
		res.json({error:0});
		res.status(200);
	});
}
function home() {
	router.get('/',function(req,res,next) {
		res.render('index');
	});
}
function help() {
	router.get('/help',function(req,res,next) {
		res.render('help');
	});
}
function map() {
	router.get('/map',function(req,res,next) {
		res.render('map');
	});
}
function signupview() {
	router.get('/sign-up',function(req,res,next) {
		res.render('sign-up');
	});
}
function brand() {
	router.get('/brand',function(req,res,next) {
		res.render('brand');
	});
}
function contact() {
	router.get('/contact',function(req,res,next) {
		res.render('contact');
	});
}
function about() {
	router.get('/about',function(req,res,next) {
		res.render('about');
	});
}


module.exports = router;
