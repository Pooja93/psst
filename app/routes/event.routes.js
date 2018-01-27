module.exports = function (app) {

    var events = require('../controllers/event.controller.js');

    // Create a new Note
    app.post('/events', events.create);

    // Retrieve all events
    app.get('/events', events.findAll);

    // Retrieve a single Note with noteId
    app.get('/events/:noteId', events.findOne);

    // Update a Note with noteId
    app.put('/events/:noteId', events.update);

    // Delete a Note with noteId
    app.delete('/events/:noteId', events.delete);
}