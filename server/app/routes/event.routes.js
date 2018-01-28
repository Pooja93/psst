module.exports = function (app) {

    var events = require('../controllers/event.controller.js');

    // Create a new Event
    app.post('/events/new', events.create);

    // Retrieve all events
    app.get('/events', events.findAll);

    // Retrieve a single Event with eventId
    app.get('/events/:eventId', events.findOne);

    // Update a Event with eventId
    app.put('/events/update/:eventId', events.update);

    // Delete a Event with eventId
    app.delete('/events/delete/:eventId', events.delete);
}