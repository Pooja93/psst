var Event = require('../models/event.model.js');

exports.create = function (req, res) {
    // Create and Save a new Event
    var event = new Event({ title: req.body.title || "Untitled Event", description: req.body.description, lattitude: req.body.lattitude, longitude: req.body.longitude, votes: req.body.votes });

    event.save(function (err, data) {
        console.log(data);
        if (err) {
            console.log(err);
            res.status(500).send({ message: "Some error ocuured while creating the Event." });
        } else {
            res.send(data);
        }
    });
};

exports.findAll = function (req, res) {
    // Retrieve and return all events from the database.
    Event.find(function (err, events) {
        if (err) {
            res.status(500).send({ message: "Some error ocuured while retrieving events." });
        } else {
            res.send(events);
        }
    });
};

exports.findOne = function (req, res) {
    // Find a single event with a eventId
    Event.findById(req.params.eventId, function (err, data) {
        if (err) {
            res.status(500).send({ message: "Could not retrieve event with id " + req.params.eventId });
        } else {
            res.send(data);
        }
    });
};

exports.update = function (req, res) {
    // Update a event identified by the eventId in the request
    Event.findById(req.params.eventId, function (err, event) {
        if (err) {
            res.status(500).send({ message: "Could not find a event with id " + req.params.eventId });
        }

        event.title = req.body.title;
        event.content = req.body.content;
        event.votes = req.body.votes;
        event.lattitude = req.body.lattitude;
        event.longitude = req.body.longitude;

        event.save(function (err, data) {
            if (err) {
                res.status(500).send({ message: "Could not update event with id " + req.params.eventId });
            } else {
                res.send(data);
            }
        });
    });
};

exports.delete = function (req, res) {
    // Delete a event with the specified eventId in the request
    Event.remove({ _id: req.params.eventId }, function (err, data) {
        if (err) {
            res.status(500).send({ message: "Could not delete event with id " + req.params.id });
        } else {
            console.log(req.params.eventId)
            res.send({ message: "Event deleted successfully!" })
        }
    });
};

exports.coordinates = function (req, res) {
    // Get lattitude and longitude of an event
    Event.findById(req.params.eventId, function (err, data) {
        if (err) {
            res.status(500).send({ message: "Could not retrieve event with id " + req.params.eventId });
        } else {
            res.send({ "lattitude": req.params.lattitude, "longitude": req.params.longitude });
        }
    });
};

exports.votes = function (req, res) {
    // Get votes for an event
    console.log("hi" + req.params.votes);
    Event.findById(req.params.eventId, function (err, data) {
        if (err) {
            res.status(500).send({ message: "Could not retrieve event with id " + req.params.eventId });
        } else {
            console.log(req.params.votes);
            res.send(req.params.votes);
        }
    });
};