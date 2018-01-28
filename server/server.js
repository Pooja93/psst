var express = require('express');
var bodyParser = require('body-parser');

// create express app
var app = express();

// parse application/x-www-form-urlencoded
app.use(bodyParser.urlencoded({ extended: true }))

// parse application/json
app.use(bodyParser.json())

// Configuring the database
var dbConfig = require('./config/database.config.js');
var mongoose = require('mongoose');
mongoose.connect(dbConfig.url, (err, res) => {
    if (err) {
        console.log('ERROR connecting to: ' + dbConfig.url + '. ' + err);
    } else {
        console.log('Succeeded connected to: ' + dbConfig.url);
    }
    // useMongoClient: true
});

mongoose.connection.on('error', function () {
    console.log('Could not connect to the database. Exiting now...');
    process.exit();
});

mongoose.connection.once('open', function () {
    console.log("Successfully connected to the database");
})

// define a simple route
app.get('/', function (req, res) {
    res.json({ "message": "Welcome to the events view." });
});

require('./app/routes/event.routes.js')(app);

// listen for requests
var port = process.env.PORT || 3000;
app.listen(port, function () {
    console.log("Server is listening on port " + port);
});