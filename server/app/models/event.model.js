var mongoose = require('mongoose');

var EventSchema = mongoose.Schema({
    title: String,
    content: String
}, {
        timestamps: true
    });

module.exports = mongoose.model('Event', EventSchema);