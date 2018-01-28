# City Events API

Build a Restful CRUD API for listing all events around your city.

## Steps to Setup

1. Install dependencies

```bash
npm install
```

2. Start mongodb in another tab, entering your password if requested
```bash
sudo mongod
```

3. Run Server

```bash
npm run dev
```

You can browse the apis at <http://localhost:3000>

## API Endpoints

1. Create a new event (POST Request)
```http
http://localhost:3000/events/new
```
2. Retrieve all events (GET Request)
```http
http://localhost:3000/events
```

3. Retrieve a single Event with eventId (GET Request)
```http
http://localhost:3000/events/new/:eventId
```
4. Update a Event with eventId (PUT Request)
```http
http://localhost:3000/events/update/:eventId
```
5. Delete a Event with eventId (DELETE Request)
```http
http://localhost:3000/events/delete/:eventId
```

## Testing

Use Postman. Remember to send JSON for the GET and DELETE requests.

## Credits

Based on [This Blog Post](https://www.callicoder.com/node-js-express-mongodb-restful-crud-api-tutorial/)
